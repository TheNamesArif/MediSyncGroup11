package com.example.medisync.model;

import java.util.List;

public class Medicine {
    private String name;
    private String amount;
    private String unit;
    private String instruction;
    private List<String> intakeTimes;
    private String status;
    private String patientName;

    public Medicine() {}

    public Medicine(String name, String amount, String unit, String instruction, List<String> intakeTimes, String status, String patientName) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.instruction = instruction;
        this.intakeTimes = intakeTimes;
        this.status = status;
        this.patientName = patientName;
    }

    public String getName() { return name; }
    public String getAmount() { return amount; }
    public String getUnit() { return unit; }
    public String getInstruction() { return instruction; }
    public List<String> getIntakeTimes() { return intakeTimes; }
    public String getStatus() { return status; }
    public String getPatientName() { return patientName; }
}
