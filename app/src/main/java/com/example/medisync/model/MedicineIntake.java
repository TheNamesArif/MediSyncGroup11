package com.example.medisync.model;

public class MedicineIntake {
    private Medicine medicine;
    private String intakeTime;
    private String status;
    private int index;

    public MedicineIntake(Medicine medicine, String intakeTime, String status, int index) {
        this.medicine = medicine;
        this.intakeTime = intakeTime;
        this.status = status;
        this.index = index;
    }

    public Medicine getMedicine() { return medicine; }
    public String getIntakeTime() { return intakeTime; }
    public String getStatus() { return status; }
    public int getIndex() { return index; }
}