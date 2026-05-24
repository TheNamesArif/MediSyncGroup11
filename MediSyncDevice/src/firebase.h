#pragma once
#include <Arduino.h>
#include <vector>

struct Medicine {
  String docId;
  String name;
  String amount;
  String unit;
  String instruction;
  String remarks;
  std::vector<String> intakeTimes;  // e.g. ["10:30 pm", "08:00 am"]
  long endDateSeconds;              // unix timestamp
};

bool firebaseFetch(std::vector<Medicine>& medicines);