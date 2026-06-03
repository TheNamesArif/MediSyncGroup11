package com.example.medisync.model;

public class MedicineIntake {
    private Medicine medicine;
    private String intakeTime;

    public MedicineIntake(Medicine medicine, String intakeTime) {
        this.medicine = medicine;
        this.intakeTime = intakeTime;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public String getIntakeTime() {
        return intakeTime;
    }
}
