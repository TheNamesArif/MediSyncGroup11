#pragma once
#include <LiquidCrystal_I2C.h>

void displayInit();
void displayWelcome();
void displayConnecting();
void displayTime(String datetime);
void displayMedicine(String name, String time, String instruction, String amount, String unit);
void displayAlert(String name, String time, String instruction, String amount, String unit, String currentTime);
void displayNoMeds();
void displayFetching();
void displayError(String msg);
void displayClear();
void displayMedicineIdle(String name, String time, int current, int total);