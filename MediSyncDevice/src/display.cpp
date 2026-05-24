#include "display.h"
#include "config.h"

LiquidCrystal_I2C lcd(LCD_I2C_ADDR, LCD_COLS, LCD_ROWS);

void displayInit() {
  lcd.init();
  lcd.backlight();
}

void displayWelcome() {
  lcd.clear();
  lcd.setCursor(3, 0); lcd.print("MediSync");
  lcd.setCursor(4, 1); lcd.print("Initializing");
  lcd.setCursor(2, 3); lcd.print("Please wait...");
}

void displayConnecting() {
  lcd.clear();
  lcd.setCursor(0, 0); lcd.print("Connecting to WiFi");
  lcd.setCursor(0, 2); lcd.print("SSID: ");
  lcd.print(WIFI_SSID);
}

void displayTime(String datetime) {
  lcd.setCursor(0, 0);
  lcd.print(datetime);
  // pad to 20 chars
  int len = datetime.length();
  for (int i = len; i < 20; i++) lcd.print(" ");
}

void displayMedicine(String name, String time, String instruction, String amount, String unit) {
  lcd.clear();
  lcd.setCursor(0, 0); lcd.print("Med: "); lcd.print(name);
  lcd.setCursor(0, 1); lcd.print("Time: "); lcd.print(time);
  lcd.setCursor(0, 2); lcd.print(instruction);
  lcd.setCursor(0, 3); lcd.print(amount); lcd.print(" "); lcd.print(unit);
}

void displayAlert(String name, String time) {
  lcd.clear();
  lcd.setCursor(4, 0); lcd.print("!! ALERT !!");
  lcd.setCursor(0, 1); lcd.print("Take: "); lcd.print(name);
  lcd.setCursor(0, 2); lcd.print("Time: "); lcd.print(time);
  lcd.setCursor(0, 3); lcd.print("Press btn to confirm");
}

void displayNoMeds() {
  lcd.clear();
  lcd.setCursor(3, 1); lcd.print("No medications");
  lcd.setCursor(4, 2); lcd.print("scheduled");
}

void displayFetching() {
  lcd.clear();
  lcd.setCursor(2, 1); lcd.print("Fetching data...");
  lcd.setCursor(2, 2); lcd.print("Please wait");
}

void displayError(String msg) {
  lcd.clear();
  lcd.setCursor(0, 0); lcd.print("ERROR:");
  lcd.setCursor(0, 1); lcd.print(msg.substring(0, 20));
}

void displayClear() {
  lcd.clear();
}

void displayMedicineIdle(String name, String time, int current, int total) {
  lcd.clear();
  lcd.setCursor(0, 0); // row 0 kept for time, skip it
  lcd.setCursor(0, 1); lcd.print("Med "); lcd.print(current); lcd.print("/"); lcd.print(total); lcd.print(": "); lcd.print(name);
  lcd.setCursor(0, 2); lcd.print("Time: "); lcd.print(time);
  lcd.setCursor(0, 3); lcd.print("System OK");
}