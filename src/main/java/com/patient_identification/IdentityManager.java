package com.patient_identification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsibilities 
    * Delegating ID matching to {@link PatientIdentifier}
    * Catching and logging {@link PatientMismatchException}s so that unmatched IDs never silently corrupt data pipeline
    * Maintaining an audit log of all mismatches for later review
    * Providing a archive list of IDs that repeatedly fail to match -> might indicate misconfigured simulator / data integrity problem
 */
public class IdentityManager {

    //underlying matcher used for ID-to-record lookups 
    private final PatientIdentifier patientIdentifier;

    /**
     * Audit log of all mismatch events
     * Each entry is a human-readable string describing unmatched ID and timestamp of attempt
     * Only accessible via getMismatchLog()
     */
    private List<String> mismatchLog;

    /**
     * List of simulator IDs that have been archived after repeated mismatch failures
     * Data points from archived IDs are rejected immediately without a fresh lookup attempt
     */
    private List<Integer> archivedIds;

    //number of consecutive mismatches for a single simulator ID that triggers archive
    private static final int ARCHIVE_THRESHOLD = 3;

    /**
     * Running count of consecutive mismatches per simulator ID
     * Used to decide when to archive an ID
     */
    private java.util.Map<Integer, Integer> mismatchCounts;

    /**
     * Constructs an IdentityManager backed by given {@link PatientIdentifier}
     *
     * @param patientIdentifier identifier used for ID-to-record lookups (cannot be null)
     */
    public IdentityManager(PatientIdentifier patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
        this.mismatchLog = new ArrayList<>();
        this.archivedIds = new ArrayList<>();
        this.mismatchCounts = new java.util.HashMap<>();
    }

    /**
     * Resolves a simulator patient ID to a verified {@link HospitalPatient} record, handling all failure cases internally
     * If id is archived, null is returned immediately and no lookup is attempted 
     * If lookup fails, mismatch is logged, failure count for this id is incremented I
        * If the threshold is reached -> id is archived 
     * Returns null on any failure so that callers can skip data point safely
     *
     * @param simulatorId numeric patient ID from signal generator
     * @return matching {@link HospitalPatient} / null if id could not be resolved or is archived
     */
    public HospitalPatient resolvePatient(int simulatorId) {
        if (archivedIds.contains(simulatorId)) {
            System.err.println("[IdentityManager] Rejected data for archived ID: " + simulatorId);
            return null;
        }

        try {
            HospitalPatient patient = patientIdentifier.match(simulatorId);
            //successful match resets the failure count for this id
            mismatchCounts.remove(simulatorId);
            return patient;
        } catch (PatientMismatchException e) {
            handleMismatch(simulatorId, e.getMessage());
            return null;
        }
    }

    /**
     * Logs a mismatch event, increments the failure counter for given simulator id, and archives id if threshold is exceeded
     *
     * @param simulatorId simulator id that failed to match
     * @param reason human-readable description of failure
     */
    private void handleMismatch(int simulatorId, String reason) {
        long now = System.currentTimeMillis();
        String entry = String.format("[%d] Mismatch for simulatorId=%d: %s", now, simulatorId, reason);
        mismatchLog.add(entry);
        System.err.println("[IdentityManager] " + entry);

        int count = mismatchCounts.getOrDefault(simulatorId, 0) + 1;
        mismatchCounts.put(simulatorId, count);

        if (count >= ARCHIVE_THRESHOLD) {
            archivedIds.add(simulatorId);
            System.err.println("[IdentityManager] Archived simulatorId=" + simulatorId + " after " + count + " consecutive mismatches.");
        }
    }

    /**
     * Returns an unmodifiable view of the mismatch audit log
     * Each entry is a timestamped string describing one mismatch event
     *
     * @return unmodifiable list of mismatch log entries
     */
    public List<String> getMismatchLog() {
        return Collections.unmodifiableList(mismatchLog);
    }

    /**
     * Returns unmodifiable view of list of archived simulator IDs 
     *
     * @return unmodifiable list of archived simulator patient IDs
     */
    public List<Integer> getArchivedIds() {
        return Collections.unmodifiableList(archivedIds);
    }

    /**
     * Manually removes a simulator ID from archive list and resets its failure counter
     * Should only be called after underlying mismatch has been investigated and resolved 
     * (e.g. patient's simulator id was corrected in the hospital database)
     *
     * @param simulatorId simulator ID to release from archive
     * @return true if id was archived and has now been released, false if it was not archived
     */
    public boolean releaseFromArchive(int simulatorId) {
        boolean removed = archivedIds.remove(Integer.valueOf(simulatorId));
        if (removed) {
            mismatchCounts.remove(simulatorId);
            System.out.println("[IdentityManager] Released simulatorId=" + simulatorId + " from archive.");
        }
        return removed;
    }

    /**
     * Returns total number of mismatch events recorded since this {@code IdentityManager} was created
     *
     * @return total mismatch count
     */
    public int getTotalMismatchCount() {
        return mismatchLog.size();
    }
}