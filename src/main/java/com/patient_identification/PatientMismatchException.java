package com.patient_identification;

/**
 * Thrown when an incoming simulator patient ID cannot be matched to any record in hospital database
 * Answers question "What happens if no match is found?"
 * Checked exception 
    * Forces every caller to resolve / decide to handle problem rather than ignore it  
 * 
 * Carries unmatched simulator ID so that calling code can include it in error logs or audit trails without needing to re-examine original input
 */
public class PatientMismatchException extends Exception {

    //Simulator patient ID that could not be matched
    private final int unmatchedSimulatorId;

    /**
     * Constructs a PatientMismatchException for the given unmatched ID
     *
     * @param simulatorId simulator patient ID that had no matching hospital record
     */
    public PatientMismatchException(int simulatorId) {
        super("No hospital record found for simulator patient ID: " + simulatorId);

        this.unmatchedSimulatorId = simulatorId;
    }

    /**
     * Constructs a PatientMismatchException with a custom message 
     * (for cases where additional context is available (e.g. the data type that triggered the lookup))
     *
     * @param simulatorId simulator patient ID that had no matching hospital record
     * @param message additional context about the failure
     */
    public PatientMismatchException(int simulatorId, String message) {
        super(message);

        this.unmatchedSimulatorId = simulatorId;
    }

    /**
     * Returns simulator patient ID that could not be matched to any hospital record
     *
     * @return unmatched simulator patient ID
     */
    public int getUnmatchedSimulatorId() {
        return unmatchedSimulatorId;
    }
}