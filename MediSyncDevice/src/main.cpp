#include <Arduino.h>
#include <WiFi.h>
#include <Wire.h>
#include <RTClib.h>
#include <vector>
#include <time.h>

#include "config.h"
#include "display.h"
#include "firebase.h"
#include "alert.h"

// ─── RTC ──────────────────────────────────────────────
TwoWire rtcWire = TwoWire(1);   // second I2C bus (GPIO 18/19)
RTC_DS1307 rtc;

// ─── State ────────────────────────────────────────────
std::vector<Medicine> medicines;

unsigned long lastFetch   = 0;
unsigned long lastCheck   = 0;
unsigned long lastBlink   = 0;
bool ledState             = false;

int  alertMedIndex        = -1;
int  alertTimeIndex       = -1;
bool alreadyAlerting      = false;

extern unsigned long _alertStart;   // defined in alert.cpp

unsigned long lastScroll   = 0;  // for scrolling long medicine names on LCD
int           scrollIndex  = 0;

// ─── Helpers ──────────────────────────────────────────

String twoDigit(int n) {
  return (n < 10 ? "0" : "") + String(n);
}

void syncRTCFromNTP() {
  Serial.println("Syncing time from NTP...");
  
  // Malaysia timezone = UTC+8
  configTime(8 * 3600, 0, "pool.ntp.org", "time.nist.gov");

  struct tm timeinfo;
  int retries = 0;
  while (!getLocalTime(&timeinfo) && retries < 20) {
    delay(500);
    Serial.print(".");
    retries++;
  }

  if (retries >= 20) {
    Serial.println("\nNTP sync failed — using existing RTC time");
    return;
  }

  // Push NTP time into RTC
  rtc.adjust(DateTime(
    timeinfo.tm_year + 1900,
    timeinfo.tm_mon + 1,
    timeinfo.tm_mday,
    timeinfo.tm_hour,
    timeinfo.tm_min,
    timeinfo.tm_sec
  ));

  Serial.printf("RTC synced: %04d-%02d-%02d %02d:%02d:%02d\n",
    timeinfo.tm_year + 1900,
    timeinfo.tm_mon + 1,
    timeinfo.tm_mday,
    timeinfo.tm_hour,
    timeinfo.tm_min,
    timeinfo.tm_sec
  );
}

// Returns current time as "HH:MM AM/PM" e.g. "10:30 PM"
String getCurrentTimeStr(DateTime now) {
  int h = now.hour();
  int m = now.minute();
  String ampm = (h >= 12) ? "PM" : "AM";
  if (h > 12) h -= 12;
  if (h == 0) h = 12;
  return twoDigit(h) + ":" + twoDigit(m) + " " + ampm;
}

// Returns full datetime string for LCD row 0
String getCurrentDateTimeStr(DateTime now) {
  return twoDigit(now.day()) + "/" +
         twoDigit(now.month()) + "/" +
         twoDigit(now.year() % 100) + " " +    // % 100 gives last 2 digits of year
         twoDigit(now.hour()) + ":" +
         twoDigit(now.minute()) + ":" +
         twoDigit(now.second());
}

// Returns true if medicine's endDate has passed
bool isExpired(Medicine& med, DateTime now) {
  if (med.endDateSeconds == 0) return false;
  return (long)now.unixtime() > med.endDateSeconds;
}

// Compare "HH:MM AM/PM" strings — only match on HH:MM + AM/PM
bool timesMatch(String current, String intake) {
  // current → "10:30 PM"
  // intake  → "10:30 PM"
  return current.substring(0, 5) == intake.substring(0, 5) &&
         current.substring(6)    == intake.substring(6);
}

// ─── WiFi ─────────────────────────────────────────────

void connectWiFi() {
  displayConnecting();
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  int tries = 0;
  while (WiFi.status() != WL_CONNECTED && tries < 40) {
    delay(500);
    Serial.print(".");
    tries++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi connected: " + WiFi.localIP().toString());
  } else {
    Serial.println("\nWiFi failed — running in offline mode");
    displayError("WiFi failed");
    delay(2000);
  }
}

// ─── Setup ────────────────────────────────────────────

void setup() {
  Serial.begin(115200);

  // I2C buses
  Wire.begin(LCD_SDA, LCD_SCL);         // LCD  → GPIO 21/22
  rtcWire.begin(RTC_SDA, RTC_SCL);      // RTC  → GPIO 18/19

  // LCD
  displayInit();
  displayWelcome();
  delay(2000);

  // Alert peripherals (buzzer, LED, button, DFPlayer)
  alertInit();

  // RTC
  if (!rtc.begin(&rtcWire)) {
    Serial.println("RTC not found!");
    displayError("RTC not found");
    delay(3000);
  } else {
    if (!rtc.isrunning()) {
      Serial.println("RTC not running — setting to compile time");
      rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
    }
    Serial.println("RTC OK");
  }

  // WiFi
  connectWiFi();

  // Sync RTC time from NTP
  syncRTCFromNTP();

  // Initial Firestore fetch
  displayFetching();
  if (firebaseFetch(medicines)) {
    Serial.println("Fetched " + String(medicines.size()) + " medicine(s)");
  } else {
    Serial.println("Initial fetch failed");
  }
  displayClear();
  lastFetch = millis();
  lastCheck = millis();
}

// ─── Loop ─────────────────────────────────────────────

void loop() {
  DateTime now           = rtc.now();
  String currentTimeStr  = getCurrentTimeStr(now);
  String currentDTStr    = getCurrentDateTimeStr(now);

  // ── Active alert handling ────────────────────────────
  if (alreadyAlerting) {
    if (millis() - lastBlink > 300) {
      ledState = !ledState;
      digitalWrite(PIN_LED, ledState);
      lastBlink = millis();
    }
    if (digitalRead(PIN_BUTTON) == LOW) {
      delay(50);
      if (digitalRead(PIN_BUTTON) == LOW) {
        alertStop();
        alreadyAlerting = false;
        Serial.println("Alert dismissed by button");
        if (alertMedIndex >= 0 && alertMedIndex < (int)medicines.size()) {
          Medicine& m = medicines[alertMedIndex];
          displayMedicine(m.name, m.intakeTimes[alertTimeIndex],
                          m.instruction, m.amount, m.unit);
        }
        delay(500);
      }
    }
    if (millis() - _alertStart > ALERT_AUTO_STOP_MS) {
      alertStop();
      alreadyAlerting = false;
      Serial.println("Alert auto-stopped");
    }
    return;
  }

  // ── Update clock on LCD row 0 ────────────────────────
  displayTime(currentDTStr);

  // ── Scroll medicines on LCD ──────────────────────────
  if (!medicines.empty() && millis() - lastScroll > 5000) {
    lastScroll = millis();
    if (scrollIndex >= (int)medicines.size()) scrollIndex = 0;
    Medicine& m = medicines[scrollIndex];
    String timeStr = m.intakeTimes.empty() ? "N/A" : m.intakeTimes[0];
    displayMedicineIdle(m.name, timeStr, scrollIndex + 1, medicines.size());
    displayTime(currentDTStr);
    scrollIndex++;
  }

  // ── Re-fetch Firestore (non-blocking check) ──────────
  if (millis() - lastFetch > FETCH_INTERVAL_MS) {
    lastFetch = millis();    // ← reset BEFORE fetch so it doesn't block check
    displayFetching();
    if (firebaseFetch(medicines)) {
      Serial.println("Re-fetched: " + String(medicines.size()) + " medicine(s)");
    } else {
      Serial.println("Re-fetch failed, using cached data");
    }
    displayClear();
  }

  // ── Check due medicines (RTC-driven, independent) ────
  if (millis() - lastCheck > CHECK_INTERVAL_MS) {
    lastCheck = millis();    // ← reset BEFORE logic
    Serial.println("Checking meds at: " + currentTimeStr);

    bool triggered = false;
    for (int i = 0; i < (int)medicines.size(); i++) {
      Medicine& med = medicines[i];

      if (isExpired(med, now)) {
        Serial.println("Skipping expired: " + med.name);
        continue;
      }

      for (int j = 0; j < (int)med.intakeTimes.size(); j++) {
        Serial.println("  Comparing: [" + currentTimeStr + "] vs [" + med.intakeTimes[j] + "]");
        if (timesMatch(currentTimeStr, med.intakeTimes[j])) {
          Serial.println("MATCH → triggering alert for: " + med.name);
          alertMedIndex   = i;
          alertTimeIndex  = j;
          alreadyAlerting = true;
          alertTrigger();
          displayAlert(med.name, med.intakeTimes[j]);
          triggered = true;
          break;
        }
      }
      if (triggered) break;
    }
  }

  // ── Re-fetch Firestore (only if WiFi connected) ──────
  if (millis() - lastFetch > FETCH_INTERVAL_MS) {
    lastFetch = millis();
    if (WiFi.status() == WL_CONNECTED) {    // ← add this check
      displayFetching();
      if (firebaseFetch(medicines)) {
        Serial.println("Re-fetched: " + String(medicines.size()) + " medicine(s)");
      } else {
        Serial.println("Re-fetch failed, using cached data");
      }
      displayClear();
    } else {
      Serial.println("WiFi down — skipping fetch, using cached data");
    }
  }

  delay(500);
}