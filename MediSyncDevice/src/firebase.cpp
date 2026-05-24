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
  DynamicJsonDocument doc(8192);  // increase if you have many medicines
  DeserializationError err = deserializeJson(doc, payload);
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

    // intakeTimes array
    JsonArray times = fields["intakeTimes"]["arrayValue"]["values"].as<JsonArray>();
    for (JsonObject tv : times) {
      String t = tv["stringValue"].as<String>();
      med.intakeTimes.push_back(normalizeTime(t));
    }

    medicines.push_back(med);
    Serial.println("Loaded: " + med.name + " | times: " + med.intakeTimes.size());
  }

  return true;
}