package com.alerts;

public interface Alert {
    String getPatientId();
    String getCondition();
    long getTimestamp();
    String getMessage();
}