package com.example.medisync.model;

import java.util.Map;

public class Medicine {
    private String documentId;
    private String name;
    private String amount;
    private String unit;
    private String instruction;
    private Map<String, String> intakeTimes; // Map where key is time (e.g. "10:30 am") and value is status
    private String patientName;
    private String patientUid;
    private String status; // Kept for overall status if needed, or backward compatibility

    public Medicine() {}

    public Medicine(String documentId, String name, String amount, String unit, String instruction,
                    Map<String, String> intakeTimes, String patientName, String patientUid) {
        this.documentId = documentId;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.instruction = instruction;
        this.intakeTimes = intakeTimes;
        this.patientName = patientName;
        this.patientUid = patientUid;
    }

    public String getDocumentId() { return documentId; }
    public String getName() { return name; }
    public String getAmount() { return amount; }
    public String getUnit() { return unit; }
    public String getInstruction() { return instruction; }
    public Map<String, String> getIntakeTimes() { return intakeTimes; }
    public String getPatientName() { return patientName; }
    public String getPatientUid() { return patientUid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}