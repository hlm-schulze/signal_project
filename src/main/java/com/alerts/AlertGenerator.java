package com.alerts;

import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 * When a condition is met, an {@link Alert} is created and dispatched via
 * the {@link AlertManager}.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private AlertManager alertManager; 

    //thresholds for evaluations 
    private static final double SYSTOLIC_HIGH = 180.0;
    private static final double SYSTOLIC_LOW = 90.0;
    private static final double DIASTOLIC_HIGH = 120.0;
    private static final double DIASTOLIC_LOW = 60.0;
    private static final double SATURATION_LOW = 92.0;  // Below 92% = hypoxemia risk
    private static final double ECG_HIGH = 1.5;
    private static final double ECG_LOW = -1.5;

    //we need to look back on patient records so set a fixed time to look back on to not take too long 
     private static final long EVALUATION_WINDOW_MS = 60_000L; // at least 60s

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient data
     * @param alertManager the alert manager responsible for dispatching alerts
     */
    public AlertGenerator(DataStorage dataStorage, AlertManager alertManager) {
        this.dataStorage = dataStorage;
        this.alertManager = alertManager;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        // Implementation goes here
        long now = System.currentTimeMillis();
        long startTime = now - EVALUATION_WINDOW_MS;
 
        int patientId = patient.getPatientId();
        List<PatientRecord> records = dataStorage.getRecords(patientId, startTime, now);

        for (PatientRecord record : records) {
            String type = record.getRecordType();
            double value = record.getMeasurementValue();
            long ts = record.getTimestamp();
 
            switch (type) {
                case "SystolicPressure":
                    if (value > SYSTOLIC_HIGH) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Critical high systolic pressure: " + value + " mmHg",
                            ts));
                    } else if (value < SYSTOLIC_LOW) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Critical low systolic pressure: " + value + " mmHg",
                            ts));
                    }
                    break;
                case "DiastolicPressure":
                    if (value > DIASTOLIC_HIGH) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Critical high diastolic pressure: " + value + " mmHg",
                            ts));
                    } else if (value < DIASTOLIC_LOW) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Critical low diastolic pressure: " + value + " mmHg",
                            ts));
                    }
                    break;
                case "Saturation":
                    // Value is stored as a percentage string like "95.0%"; parse carefully
                    double saturation = parseSaturation(value);
                    if (saturation < SATURATION_LOW) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Low blood oxygen saturation: " + saturation + "%",
                            ts));
                    }
                    break;
                case "ECG":
                    if (value > ECG_HIGH || value < ECG_LOW) {
                        triggerAlert(new Alert(
                            String.valueOf(patientId),
                            "Abnormal ECG value: " + value,
                            ts));
                    }
                    break;
                default:
                    // Other record types (Cholesterol, WhiteBloodCells, etc.) can be added when thresholds are defined
                    break;
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument. -> done is separate class {@link AlertManager}
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        alertManager.dispatchAlert(alert); 
    }

    /**
     * Helps to safely parse saturation values
     *
     * @param value raw measurement value
     * @return saturation as a plain double
     */
    private double parseSaturation(double value) {
        // measurementValue is a double -> no parsing needed 
        return value;
    }
}
