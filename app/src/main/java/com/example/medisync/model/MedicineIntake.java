package com.example.medisync.model;

public class MedicineIntake {
    private Medicine medicine;
    private String intakeTime;
    private String status;

    public MedicineIntake(Medicine medicine, String intakeTime, String status) {
        this.medicine = medicine;
        this.intakeTime = intakeTime;
        this.status = status;
    }

    public Medicine getMedicine() { return medicine; }
    public String getIntakeTime() { return intakeTime; }
    public String getStatus() { return status; }
}