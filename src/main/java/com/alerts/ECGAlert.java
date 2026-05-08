package com.alerts;

public class ECGAlert extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {

        return new BaseAlert(patientId, "ECG: " + condition, timestamp);
    }
}