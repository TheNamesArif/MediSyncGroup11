#pragma once

// ─── WiFi ────────────────────────────────────────────
#define WIFI_SSID       "WIFI KSJ_2.4G"  // Very unsafe to hardcode credentials, but this is just a demo project :)
#define WIFI_PASSWORD   "Aishh06_"

// ─── Firebase ────────────────────────────────────────
#define FIREBASE_API_KEY   "AIzaSyBsVIf1aIYb3Sey3etQ7XJwo3uCllVeXFU"
#define PROJECT_ID         "medisync-8c319"
#define USER_ID            "fMTEj0Wv6vfPx9k6jKQkHeNSJKt2"

// ─── LCD I2C (Wire  → GPIO 13 SDA, GPIO 12 SCL) ──────
#define LCD_SDA       13
#define LCD_SCL       12
#define LCD_I2C_ADDR  0x27
#define LCD_COLS      20
#define LCD_ROWS      4

// ─── RTC I2C (Wire1 → GPIO 18 SDA, GPIO 19 SCL) ──────
#define RTC_SDA       18
#define RTC_SCL       19

// ─── Pins ─────────────────────────────────────────────
#define PIN_BUZZER    5
#define PIN_LED       27
#define PIN_BUTTON    14
#define PIN_DF_RX     25   // DFPlayer TX → ESP32
#define PIN_DF_TX     26   // ESP32 TX    → DFPlayer RX

// ─── Alert timing ─────────────────────────────────────
#define ALERT_AUTO_STOP_MS   300000  // 5 minutes auto stop
#define FETCH_INTERVAL_MS    60000   // re-fetch every 60s
#define CHECK_INTERVAL_MS    30000   // check due meds every 30s