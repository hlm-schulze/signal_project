package com.data_access;

/**
 * Represents one successfully parsed data point from any input source
 *
 * After {@link DataParser} processes a raw message string, it returns a ParsedRecord containing fields other files need: 
    * PATIENT_ID, TIMESTAMP, LABEL, value 
 * {@link DataSourceAdapter} then converts this into a call to {@link com.data_management.DataStorage#addPatientData}
 *
 * Separates parsing step from storage step:
    * DataParser does not need to know about DataStorage 
    * and DataSourceAdapter does not need to know about raw message formats
 */
public class ParsedRecord {

    //Simulator-side patient ID extracted from raw message
    private final int PATIENT_ID;

    //Timestamp (milliseconds) extracted from raw message
    private final long TIMESTAMP;

    //Data label (e.g. "ECG", "SystolicPressure")
    private final String LABEL;

    //Numerical measurement value extracted from raw message
    private final double MEASUREMENT_VALUE;

    /**
     * Constructs a ParsedRecord with given field values
     *
     * @param PATIENT_ID simulator patient ID
     * @param TIMESTAMP measurement timestamp in milliseconds 
     * @param LABEL data label (e.g. "ECG")
     * @param MEASUREMENT_VALUE numerical measurement value
     */
    public ParsedRecord(int patientID, long timestamp, String label, double measurementValue) {
        this.PATIENT_ID = patientID;
        this.TIMESTAMP = timestamp;
        this.LABEL = label;
        this.MEASUREMENT_VALUE = measurementValue;
    }

    /**
     * Returns simulator patient ID
     *
     * @return patient ID
     */
    public int getPatientId() {
        return PATIENT_ID;
    }

    /**
     * Returns measurement timestamp in milliseconds
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return TIMESTAMP;
    }

    /**
     * Returns data label identifying type of measurement
     *
     * @return label (e.g. "ECG")
     */
    public String getLabel() {
        return LABEL;
    }

    /**
     * Returns numerical measurement value
     *
     * @return measurement value
     */
    public double getMeasurementValue() {
        return MEASUREMENT_VALUE;
    }

    /**
     * Returns a human-readable summary of current record
     *
     * @return formatted string with all four fields (patientID, etc.)
     */
    @Override
    public String toString() {
        return String.format("ParsedRecord{patientID=%d, timestamp=%d, label='%s', value=%.4f}",
                PATIENT_ID, TIMESTAMP, LABEL, MEASUREMENT_VALUE);
    }
}
