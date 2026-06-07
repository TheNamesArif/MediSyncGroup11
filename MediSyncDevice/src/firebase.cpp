#include "firebase.h"
#include "config.h"
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>

// Parses "10:30 pm" or "08:00 am" → "10:30 PM" normalized string
String normalizeTime(String t) {
  t.trim();
  t.toUpperCase();
  return t;
}

bool firebaseFetch(std::vector<Medicine>& medicines) {
  medicines.clear();

  WiFiClientSecure client;
  client.setInsecure();  // skip SSL cert verification (fine for dev)

  HTTPClient http;

  String url = "https://firestore.googleapis.com/v1/projects/";
  url += PROJECT_ID;
  url += "/databases/(default)/documents/users/";
  url += USER_ID;
  url += "/medicines?key=";
  url += FIREBASE_API_KEY;

  Serial.println("Fetching: " + url);
  http.begin(client, url);
  http.setTimeout(10000);
  int code = http.GET();
  Serial.println("HTTP code: " + String(code));

  if (code != 200) {
    Serial.println("Fetch failed: " + http.getString());
    http.end();
    return false;
  }

  String payload = http.getString();
  http.end();

  // Parse JSON
  DynamicJsonDocument doc(16384);
  DeserializationError err = deserializeJson(doc, payload, DeserializationOption::NestingLimit(32));
  if (err) {
    Serial.println("JSON parse error: " + String(err.c_str()));
    return false;
  }

  JsonArray docs = doc["documents"].as<JsonArray>();
  if (docs.isNull()) {
    Serial.println("No documents found");
    return false;
  }

  for (JsonObject d : docs) {
    Medicine med;

    // Extract doc ID from name path
    String fullPath = d["name"].as<String>();
    med.docId = fullPath.substring(fullPath.lastIndexOf('/') + 1);

    JsonObject fields = d["fields"];

    // Simple string fields
    if (fields["name"]["stringValue"])
      med.name = fields["name"]["stringValue"].as<String>();

    if (fields["amount"]["stringValue"])
      med.amount = fields["amount"]["stringValue"].as<String>();

    if (fields["unit"]["stringValue"])
      med.unit = fields["unit"]["stringValue"].as<String>();

    if (fields["instruction"]["stringValue"])
      med.instruction = fields["instruction"]["stringValue"].as<String>();

    if (fields["remarks"]["stringValue"])
      med.remarks = fields["remarks"]["stringValue"].as<String>();

    // endDate as Firestore timestamp → unix seconds
    if (fields["endDate"]["timestampValue"]) {
      String ts = fields["endDate"]["timestampValue"].as<String>();
      // format: "2026-05-31T06:26:39Z"
      struct tm t = {};
      strptime(ts.c_str(), "%Y-%m-%dT%H:%M:%SZ", &t);
      med.endDateSeconds = mktime(&t);
    }

    // intakeTimes: mapValue { "0": mapValue { time, status }, "1": ... }
    JsonObject outerMap = fields["intakeTimes"]["mapValue"]["fields"].as<JsonObject>();
    for (JsonPair kv : outerMap) {
      JsonObject inner  = kv.value()["mapValue"]["fields"].as<JsonObject>();
      String timeKey    = normalizeTime(inner["time"]["stringValue"].as<String>());
      String status     = inner["status"]["stringValue"].as<String>();
      med.intakeTimes[timeKey] = status;
      Serial.println("  Time: " + timeKey + " → " + status);
    }

    medicines.push_back(med);
    Serial.println("Loaded: " + med.name + " | times: " + med.intakeTimes.size());
  }

  return true;
}

// ── PATCH a single intakeTimes entry to "taken" ───────────────────────────────
bool firebaseUpdateIntakeStatus(const Medicine& med, const String& timeKey, const String& status) {
  WiFiClientSecure client;
  client.setInsecure();

  HTTPClient http;

  // Firestore field path uses dot notation; the time key contains spaces/colons
  // so we use the updateMask approach with the full intakeTimes map re-sent.
  // We PATCH only the intakeTimes field using a field-level update mask.
  // URL: PATCH .../documents/users/{uid}/medicines/{docId}?updateMask.fieldPaths=intakeTimes&key=...
  String url = "https://firestore.googleapis.com/v1/projects/";
  url += PROJECT_ID;
  url += "/databases/(default)/documents/users/";
  url += USER_ID;
  url += "/medicines/";
  url += med.docId;
  url += "?updateMask.fieldPaths=intakeTimes&key=";
  url += FIREBASE_API_KEY;

  // Build the full intakeTimes mapValue with the updated entry.
  // Structure: { "0": mapValue { time, status }, "1": mapValue { time, status }, ... }
  // We must resend all entries because Firestore replaces the whole field.
  DynamicJsonDocument body(4096);
  JsonObject fields   = body.createNestedObject("fields");
  JsonObject itField  = fields.createNestedObject("intakeTimes");
  JsonObject outerMap = itField.createNestedObject("mapValue");
  JsonObject outerFlds = outerMap.createNestedObject("fields");

  int idx = 0;
  for (auto& kv : med.intakeTimes) {
    String val = (kv.first == timeKey) ? status : kv.second;
    String idxKey = String(idx++);
    JsonObject inner     = outerFlds.createNestedObject(idxKey);
    JsonObject innerMap  = inner.createNestedObject("mapValue");
    JsonObject innerFlds = innerMap.createNestedObject("fields");
    innerFlds["time"]["stringValue"]   = kv.first;
    innerFlds["status"]["stringValue"] = val;
  }

  String bodyStr;
  serializeJson(body, bodyStr);

  Serial.println("PATCH " + url);
  Serial.println("Body: " + bodyStr);

  http.begin(client, url);
  http.setTimeout(10000);
  http.addHeader("Content-Type", "application/json");
  int code = http.PATCH(bodyStr);
  String resp = http.getString();
  http.end();

  Serial.println("PATCH code: " + String(code));
  if (code != 200) {
    Serial.println("PATCH failed: " + resp);
    return false;
  }
  return true;
}