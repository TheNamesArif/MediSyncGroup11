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
  lcd.setCursor(0, 2); lcd.print("Initializing");
  lcd.setCursor(0, 3); lcd.print("Please wait...");
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
  lcd.setCursor(0, 2); lcd.print(instruction); lcd.print("  "); lcd.print(amount); lcd.print(" "); lcd.print(unit);
  lcd.setCursor(0, 3); lcd.print("Press again if taken");
}

void displayAlert(String name, String time, String instruction, String amount, String unit, String currentTime) {
  lcd.clear();

  // Row 0: "!! ALERT !!  14:32"
  String row0 = "!! ALERT !!  " + currentTime.substring(0, 5); // "HH:MM" only
  lcd.setCursor(0, 0); lcd.print(row0);

  // Row 1: medicine name (truncated to 20 chars)
  String row1 = "Take: " + name;
  lcd.setCursor(0, 1); lcd.print(row1.substring(0, 20));

  // Row 2: instruction | amount+unit (truncated to fit)
  String row2 = instruction + "  " + amount + unit;
  if (row2.length() > 20) row2 = row2.substring(0, 20);
  lcd.setCursor(0, 2); lcd.print(row2);

  // Row 3: fixed prompt
  lcd.setCursor(0, 3); lcd.print("Press btn to dismiss");
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
  lcd.setCursor(0, 1); lcd.print("Med "); lcd.print(current); lcd.print("/"); lcd.print(total); lcd.print(": "); lcd.print(name);
  lcd.setCursor(0, 2); lcd.print("Next Intake:"); 
  lcd.setCursor(0, 3); lcd.print(time);
}