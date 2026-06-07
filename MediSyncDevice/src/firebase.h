#pragma once
#include <Arduino.h>
#include <vector>
#include <map>

struct Medicine {
  String docId;
  String name;
  String amount;
  String unit;
  String instruction;
  String remarks;
  // key = normalized time "HH:MM AM/PM", value = "pending" | "taken"
  std::map<String, String> intakeTimes;
  long endDateSeconds;              // unix timestamp
};

bool firebaseFetch(std::vector<Medicine>& medicines);

// Marks a single intake time as "taken" in Firestore (PATCH)
bool firebaseUpdateIntakeStatus(const Medicine& med, const String& timeKey, const String& status);