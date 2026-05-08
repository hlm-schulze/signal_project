package com.alerts;

import java.util.ArrayList;
import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Responsible for monitoring patient data and generating alerts when certain predefined conditions are met
 * Relies on a {@link DataStorage} instance to access patient data and evaluate it against specific health criteria
 * When a condition is met, an {@link Alert} is created and dispatched via {@link AlertManager}
 * 
 * Throughout this class, we make the assumption that the recors are chronologically ordered to make accessinf and triggering alerts easier
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private AlertManager alertManager;

    //thresholds for evaluations
    private static final double SYSTOLIC_HIGH = 180.0;
    private static final double SYSTOLIC_LOW = 90.0;
    private static final double DIASTOLIC_HIGH = 120.0;
    private static final double DIASTOLIC_LOW = 60.0;
    private static final double SATURATION_LOW = 92.0; // Below 92% = hypoxemia risk

    //New
    private static final double BP_TREND_DELTA = 10.0; //Blood pressure trend: each of 3 consecutive readings must change by more than this
    private static final double SATURATION_RAPID_DROP = 5.0; //Rapid drop alert: saturation falls by this many percent within the rapid-drop window
    private static final long SATURATION_RAPID_WINDOW_MS = 10 * 60 * 1000L; //Time window for rapid-saturation-drop check (10 minutes in ms)
    private static final int ECG_WINDOW_SIZE = 20; //Number of ECG samples used in the sliding window for computing the average against which peaks are compared
    private static final double ECG_PEAK_FACTOR = 2.0; //Peak is considered abnormal when its absolute value exceeds the sliding-window mean by more than this multiplier

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
     * Evaluates all recent records for given patient and triggers any alerts whose conditions are met
     *
     * Method fetches records from last EVALUATION_WINDOW_MS ms,
     * separates them by type,
     * and delegates to type-specific check methods
     *
     * @param patient patient whose data should be evaluated
     */
    public void evaluateData(Patient patient) {
        long now = System.currentTimeMillis();
        long startTime = now - EVALUATION_WINDOW_MS;
        int patientId = patient.getPatientId();

        List<PatientRecord> records = dataStorage.getRecords(patientId, startTime, now);

        //Have separate records storing (done by type) for multi-record checks
        List<PatientRecord> systolicRecords = new ArrayList<>();
        List<PatientRecord> diastolicRecords = new ArrayList<>();
        List<PatientRecord> saturationRecords = new ArrayList<>();
        List<PatientRecord> ecgRecords = new ArrayList<>();

        for (PatientRecord record : records) {
            switch (record.getRecordType()) {
                case "SystolicPressure":
                    systolicRecords.add(record);
                    break;
                case "DiastolicPressure":
                    diastolicRecords.add(record);
                    break;
                case "Saturation":
                    saturationRecords.add(record);
                    break;
                case "ECG":
                    ecgRecords.add(record);
                    break;
                case "Alert":
                    checkTriggeredAlert(record, patientId);
                    break;
                default:
                    break; //could theoretically also implement other values like WhiteBloodCells
            }
        }

        checkBloodPressureCritical(systolicRecords, patientId, "Systolic", SYSTOLIC_HIGH, SYSTOLIC_LOW);
        checkBloodPressureCritical(diastolicRecords, patientId, "Diastolic", DIASTOLIC_HIGH, DIASTOLIC_LOW);
        checkBloodPressureTrend(systolicRecords, patientId, "Systolic");
        checkBloodPressureTrend(diastolicRecords, patientId, "Diastolic");
        checkSaturationLow(saturationRecords, patientId);
        checkSaturationRapidDrop(saturationRecords, patientId);
        checkHypotensiveHypoxemia(systolicRecords, saturationRecords, patientId);
        checkEcgPeaks(ecgRecords, patientId);
    }

    /**
     * Triggers an alert for any single reading that crosses a critical threshold for BloodPressure
     *
     * @param records list of records for one blood pressure type (systolic or diastolic)
     * @param patientId patient's ID
     * @param label human-readable label used in the alert message ("Systolic" / "Diastolic")
     * @param high upper critical threshold
     * @param low lower critical threshold
     */
    private void checkBloodPressureCritical(List<PatientRecord> records, int patientId, String label, double high, double low) {
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (value > high) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        "Critical high " + label + " pressure: " + value + " mmHg",
                        record.getTimestamp()));
            } else if (value < low) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        "Critical low " + label + " pressure: " + value + " mmHg",
                        record.getTimestamp()));
            }
        }
    }

    /**
     * Blood Pressure
     * Triggers an alert when three consecutive blood pressure readings each change by more than one BP_TREND_DELTA mmHg in the same direction
     * (all increasing or all decreasing)
     *
     * Records are evaluated in the order they appear in list
     * (we assume that the list storing them is chronological)
     *
     * @param records chronologically ordered list of BP records
     * @param patientId patient's ID
     * @param label blood pressure type ("Systolic" or "Diastolic")
     */
    private void checkBloodPressureTrend(List<PatientRecord> records, int patientId, String label) {
        if (records.size() < 3) {
            return;
        }

        for (int i = 2; i < records.size(); i++) {
            double v0 = records.get(i - 2).getMeasurementValue();
            double v1 = records.get(i - 1).getMeasurementValue();
            double v2 = records.get(i).getMeasurementValue();

            double delta1 = v1 - v0;
            double delta2 = v2 - v1;

            boolean increasing = delta1 > BP_TREND_DELTA && delta2 > BP_TREND_DELTA;
            boolean decreasing = delta1 < -BP_TREND_DELTA && delta2 < -BP_TREND_DELTA;

            if (increasing) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        label + " pressure increasing trend: " + v0 + " -> " + v1 + " -> " + v2 + " mmHg",
                        records.get(i).getTimestamp()));
            } else if (decreasing) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        label + " pressure decreasing trend: " + v0 + " -> " + v1 + " -> " + v2 + " mmHg",
                        records.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Blood Saturation
     * Triggers an alert for any reading where saturation falls below value of SATURATION_LOW
     *
     * @param records saturation records to evaluate
     * @param patientId patient's ID
     */
    private void checkSaturationLow(List<PatientRecord> records, int patientId) {
        for (PatientRecord record : records) {
            double saturation = parseSaturation(record.getMeasurementValue());
            if (saturation < SATURATION_LOW) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        "Low amount of oxygen -> Saturation: " + saturation + "%",
                        record.getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert when saturation drops by value of SATURATION_RAPID_DROP or more within a time window in ms defined in SATURATION_RAPID_WINDOW_MS
     *
     * For each record, this method looks back over preceding SATURATION_RAPID_WINDOW_MS ms and compares highest value seen in that window to current value
     *
     * @param records chronologically ordered saturation records
     * @param patientId patient's ID
     */
    private void checkSaturationRapidDrop(List<PatientRecord> records, int patientId) {
        for (int i = 1; i < records.size(); i++) {
            PatientRecord current = records.get(i);
            double currentValue = parseSaturation(current.getMeasurementValue());

            //Find highest saturation in the window before current reading
            double windowHigh = currentValue;
            for (int j = i - 1; j >= 0; j--) {
                PatientRecord earlier = records.get(j);
                if (current.getTimestamp() - earlier.getTimestamp() > SATURATION_RAPID_WINDOW_MS) {
                    break;
                }
                double v = parseSaturation(earlier.getMeasurementValue());
                if (v > windowHigh) {
                    windowHigh = v;
                }
            }

            if (windowHigh - currentValue >= SATURATION_RAPID_DROP) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        "Rapid saturation drop: fell from " + windowHigh + "% to "
                                + currentValue + "% within 10 minutes",
                        current.getTimestamp()));
            }
        }
    }

    /**
     * Hypotensive Hypoexemia
     * Triggers a Hypotensive Hypoxemia alert when systolic pressure is below value of SYSTOLIC_LOW
     * &&
     * saturation is below of SATURATION_LOW within same evaluation window
     *
     * @param systolicRecords systolic BP records from evaluation window
     * @param saturationRecords saturation records from evaluation window
     * @param patientId patient's ID
     */
    private void checkHypotensiveHypoxemia(List<PatientRecord> systolicRecords, List<PatientRecord> saturationRecords, int patientId) {
        boolean lowBp = systolicRecords.stream().anyMatch(r -> r.getMeasurementValue() < SYSTOLIC_LOW);
        boolean lowSat = saturationRecords.stream().anyMatch(r -> parseSaturation(r.getMeasurementValue()) < SATURATION_LOW);

        if (lowBp && lowSat) {
            long timestamp = System.currentTimeMillis();
            triggerAlert(new BaseAlert(String.valueOf(patientId),
                    "Hypotensive Hypoxemia Alert: low systolic BP and low oxygen saturation detected",
                    timestamp));
        }
    }

    /**
     * ECG Checks
     * Detects abnormal ECG peaks using a sliding window average (ECG_WINDOW_SIZE)
     * For each reading, mean of preceding ECG_WINDOW_SIZE samples is computed
     * If current reading's absolute value exceeds mean by a factor of ECG_PEAK_FACTOR -> alert is triggered
     * If fewer than ECG_WINDOW_SIZE preceding samples are available -> available samples are used instead
     * Evaluation starts from index 1 so there is always at least one preceding value at index 0
     *
     * @param records chronologically ordered ECG records
     * @param patientId patient's ID
     */
    private void checkEcgPeaks(List<PatientRecord> records, int patientId) {
        if (records.size() < 2) {
            return;
        }

        for (int i = 1; i < records.size(); i++) {
            //Build window of samples preceding position i
            int windowStart = Math.max(0, i - ECG_WINDOW_SIZE); //we make sure that we don't go out of bounds froom the records we have
            List<PatientRecord> window = records.subList(windowStart, i);

            double mean = window.stream()
                    .mapToDouble(PatientRecord::getMeasurementValue)
                    .average()
                    .orElse(0.0); //makes sure we don't get an error if window is empty

            double currentValue = records.get(i).getMeasurementValue();

            //Alert when peak deviates significantly from average we calculated before
            if (Math.abs(currentValue) > Math.abs(mean) * ECG_PEAK_FACTOR && Math.abs(mean) > 0.0) {
                triggerAlert(new BaseAlert(String.valueOf(patientId),
                        "Abnormal ECG peak detected: value=" + currentValue + " (window avg=" + String.format("%.3f", mean) + ")",
                        records.get(i).getTimestamp()));
            }
        }
    }

    /**
     * Handles an "Alert" record emitted by {@code AlertGenerator} data generator (nurse/patient bed button) -> "manually" triggered alert
     * If record's data value is "triggered" alert is forwarded to {@link AlertManager}
     * Records with value "resolved" are silently ignored here (managed by {@link AlertManager#resolveAlert})
     *
     * @param record alert record from data stream
     * @param patientId patient's ID
     */
    private void checkTriggeredAlert(PatientRecord record, int patientId) {
        //Data generator stores "triggered" / "resolved" as a numeric 0/1, but FileOutputStrategy writes it as a string
        //We treat any non-zero measurement value as "triggered".
        if (record.getMeasurementValue() != 0.0) {
            triggerAlert(new BaseAlert(String.valueOf(patientId),
                    "Manual alert triggered by someone",
                    record.getTimestamp()));
        }
    }

    /**
     * Triggers an alert for monitoring system. This method can be extended to
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
