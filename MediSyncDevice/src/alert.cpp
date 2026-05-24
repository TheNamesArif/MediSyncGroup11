#include "alert.h"
#include "config.h"

HardwareSerial dfSerial(2);  // UART2
DFRobotDFPlayerMini dfPlayer;

bool _alertActive = false;
unsigned long _alertStart = 0;

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
  _alertActive = true;
  _alertStart = millis();
  digitalWrite(PIN_LED, HIGH);
  digitalWrite(PIN_BUZZER, HIGH);
  dfPlayer.play(1);  // plays 0001.mp3 from SD card
}

void alertStop() {
  _alertActive = false;
  digitalWrite(PIN_LED, LOW);
  digitalWrite(PIN_BUZZER, LOW);
  dfPlayer.stop();
}

bool alertIsActive() {
  return _alertActive;
}