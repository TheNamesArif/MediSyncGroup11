#include "alert.h"
#include "config.h"

HardwareSerial dfSerial(2);  // UART2
DFRobotDFPlayerMini dfPlayer;

bool _alertActive = false;
unsigned long _alertStart = 0;

bool _buzzerState       = false;
unsigned long _lastBeep = 0;
#define BEEP_ON_MS  300   // beep duration
#define BEEP_OFF_MS 200   // gap between beeps

void alertInit() {
  pinMode(PIN_BUZZER, OUTPUT);
  pinMode(PIN_LED, OUTPUT);
  pinMode(PIN_BUTTON, INPUT_PULLUP);
  digitalWrite(PIN_BUZZER, LOW);
  digitalWrite(PIN_LED, LOW);

  dfSerial.begin(9600, SERIAL_8N1, PIN_DF_RX, PIN_DF_TX);
  delay(1000);
  if (dfPlayer.begin(dfSerial)) {
    dfPlayer.volume(25);  // 0-30
    Serial.println("DFPlayer ready");
  } else {
    Serial.println("DFPlayer not found - continuing without audio");
  }
}

void alertTrigger() {
  _alertActive  = true;
  _alertStart   = millis();
  _lastBeep     = millis();
  _buzzerState  = true;
  digitalWrite(PIN_BUZZER, HIGH);   // start first beep immediately
  dfPlayer.loop(1);
}

void alertStop() {
  _alertActive = false;
  digitalWrite(PIN_BUZZER, LOW);
  digitalWrite(PIN_LED, LOW);
  _buzzerState = false;
  dfPlayer.stop();
}

bool alertIsActive() {
  return _alertActive;
}

// Call this every loop() iteration while alert is active
void alertUpdate() {
  if (!_alertActive) return;
  unsigned long now = millis();
  if (_buzzerState && now - _lastBeep > BEEP_ON_MS) {
    digitalWrite(PIN_BUZZER, LOW);
    _buzzerState = false;
    _lastBeep    = now;
  } else if (!_buzzerState && now - _lastBeep > BEEP_OFF_MS) {
    digitalWrite(PIN_BUZZER, HIGH);
    _buzzerState = true;
    _lastBeep    = now;
  }
}

void alertPlayConfirm() {
  dfPlayer.play(2);  // plays track 2 once, no loop
}