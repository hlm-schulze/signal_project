package com.alerts;

public class BloodOxygenAlert extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition,long timestamp) {

        return new BaseAlert( patientId, "Blood Oxygen: " + condition, timestamp);
    }
}