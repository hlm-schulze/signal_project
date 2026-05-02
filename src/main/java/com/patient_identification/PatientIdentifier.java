package com.patient_identification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Matches incoming simulator patient IDs to their corresponding {@link HospitalPatient} records from hospital database
 * Single class that resolves id to hospital id for other classes 
 * This ensures that the data/readings/alerts are assigned to correct patient
 * 
 * Hospital database represented by an in-memory map 
 * In a real world scenario this would be replaced by a database query / hospital information system (HIS) API call,
 */
public class PatientIdentifier {

    //Internal lookup table mapping simulator patient IDs to their corresponding hospital records  
    private final Map<Integer, HospitalPatient> idLookup;

    /**
     * Constructs a PatientIdentifier pre-loaded with given list of hospital patients
     * Each patient's simulatorId is used as lookup key
     *
     * @param hospitalPatients list of known hospital patients (cannot be null but can be empty)
     */
    public PatientIdentifier(List<HospitalPatient> hospitalPatients) {
        this.idLookup = new HashMap<>();

        for (HospitalPatient hp : hospitalPatients) {
            idLookup.put(hp.getSimulatorId(), hp);
        }
    }

    /**
     * Attempts to match given simulator patient ID to a hospital record
     * If a match is found, corresponding {@link HospitalPatient} is returned
     * If no match exists, a {@link PatientMismatchException} is thrown 
        * Callers must handle this explicitly rather than receiving a silent "null"
     *
     * @param simulatorId numeric patient ID as produced by the signal generator
     * @return {@link HospitalPatient} record linked to this simulator ID
     * @throws PatientMismatchException if no hospital record exists for given simulator ID
     */
    public HospitalPatient match(int simulatorId) throws PatientMismatchException {
        HospitalPatient patient = idLookup.get(simulatorId);

        if (patient == null) {
            throw new PatientMismatchException(simulatorId);
        }
        return patient;
    }

    /**
     * Returns true if  given simulator ID is registered in lookup table, without throwing an exception
     * Useful for pre-checks before committing to full match operation
     *
     * @param simulatorId simulator patient ID to check
     * @return true if a hospital record exists for this ID, false otherwise
     */
    public boolean isKnown(int simulatorId) {
        return idLookup.containsKey(simulatorId);
    }

    /**
     * Registers a new hospital patient in lookup table at runtime
     * (Intended for cases where patients are admitted after system starts (e.g. emergency admissions))
     *
     * @param hospitalPatient new patient to register (cannot be null)
     */
    public void register(HospitalPatient hospitalPatient) {
        idLookup.put(hospitalPatient.getSimulatorId(), hospitalPatient);
    }

    /**
     * Returns total number of hospital patients currently registered in lookup table.
     *
     * @return number of registered patients
     */
    public int getRegisteredCount() {
        return idLookup.size();
    }
}