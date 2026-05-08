package com.alerts;

public class BloodPressureAlert extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BaseAlert(patientId, "Blood Pressure: " + condition, timestamp);
    }
}