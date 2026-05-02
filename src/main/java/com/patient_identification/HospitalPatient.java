package com.patient_identification;
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
/**
 * Represents a patient's full identity record as stored in the hospital database 
 * (since file patient only stores id and record)
 * Is used to find the actual person not just the id
 * 
 * Instances of this class will be accessed through {@link PatientIdentifier} / {@link IdentityManager}
    * limits exposure of personal data
 */
public class HospitalPatient {
 
    //simulator-side numeric patient ID hospital record is linked to
    private final int simulatorId;
 
    //patient's full legal name
    private final String fullName;
 
    //patient's date of birth -> used for identity verification
    private final LocalDate dateOfBirth;
 
    /**
     * Ward or unit patient is currently admitted to (e.g. "Cardiology – Ward 4B")
     * May be {@code null} if the patient is not currently admitted
     */
    private final String admittedWard;
 
    /**
     * Chronological list of clinical notes & diagnoses representing patient's medical history
     */
    private List<String> medicalHistory;
 
    /**
     * Constructs a {@code HospitalPatient} with the given identity details
     *
     * @param simulatorId numeric ID used by data simulator to identify this patient (must be positive)
     * @param fullName patient's full legal name (cannot be null)
     * @param dateOfBirth patient's date of birth (cannot be null)
     * @param admittedWard ward patient is currently in / null if not currently admitted
     * @param medicalHistory list of clinical history notes (may be empty but cannot be null)
     */
    public HospitalPatient(int simulatorId, String fullName, LocalDate dateOfBirth,  String admittedWard, List<String> medicalHistory) {
        this.simulatorId = simulatorId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.admittedWard = admittedWard;
        this.medicalHistory = new ArrayList<>(medicalHistory);
    }
 
    /**
     * Returns simulator-side numeric ID that links this hospital record to incoming data from signal generator
     *
     * @return simulator patient ID
     */
    public int getSimulatorId() {
        return simulatorId;
    }
 
    /**
     * Returns patient's full legal name
     *
     * @return the full name (never null)
     */
    public String getFullName() {
        return fullName;
    }
 
    /**
     * Returns patient's date of birth
     *
     * @return date of birth (never null)
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
 
    /**
     * Returns ward patient is currently admitted to / null if patient is not currently admitted
     *
     * @return admitted ward / null
     */
    public String getAdmittedWard() {
        return admittedWard;
    }
 
    /**
     * Returns an unmodifiable view of patient's medical history notes
     * Calling this method makes accessing possibly but not editing 
        * Makes sure that Medical History cannot be corrupted 
     * 
     * @return unmodifiable list of clinical history entries (cannot be null)
     */
    public List<String> getMedicalHistory() {
        return Collections.unmodifiableList(medicalHistory);
    }
 
    /**
     * Returns brief, non-sensitive summary of record suitable for logging
     * Doesn't give out full name & date of birth (safety reasons)
     *
     * @return summary string containing only simulator ID and ward
     */
    @Override
    public String toString() {
        return String.format("HospitalPatient{simulatorId=%d, ward='%s'}",
                simulatorId, admittedWard != null ? admittedWard : "not admitted");
    }
}