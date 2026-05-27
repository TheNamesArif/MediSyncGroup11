#pragma once
#include <DFRobotDFPlayerMini.h>
#include <HardwareSerial.h>

void alertInit();
void alertTrigger();   // start buzzer + LED + voice
void alertStop();      // stop everything
bool alertIsActive();
void alertUpdate();   // call this every loop() iteration while alert is active