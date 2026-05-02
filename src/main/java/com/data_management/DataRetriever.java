package com.data_management;
 
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
 
/**
 * Provides dedicated query interface for medical staff to retrieve patient data from {@link DataStorage}
 *
 * Separates read concerns from storage class itself:
    * {@link DataStorage} owns persistence
    * {@code DataRetriever} owns querying 
 * Makes it easier to add new query types (e.g. "latest N records", "records by type") without touching the storage
 * 
 * In a real world scenario the callers role would be verified (e.g. doctor, nurse)
    * {@code accessRole} parameter in each method is a placeholder for that
 */
public class DataRetriever {
 
    /** The underlying storage this retriever reads from. */
    private final DataStorage dataStorage;
 
    /**
     * Constructs {@code DataRetriever} backed by given {@link DataStorage}
     *
     * @param dataStorage storage instance to query (cannot not be null)
     */
    public DataRetriever(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }
 
    /**
     * Retrieves all records for a specific patient within a given time window
     * Primary query used during real-time monitoring
     *
     * @param patientId unique identifier of patient
     * @param startTime start of time window (in milliseconds since Unix epoch)
     * @param endTime end of time window (in milliseconds since Unix epoch)
     * @param accessRole role of the requester (e.g. "doctor", "nurse") -> future access-control checks
     * @return list of PatientRecord objects within specified range, or empty list if no records are found
     */
    public List<PatientRecord> getRecordsByTimeRange(int patientId, long startTime, long endTime, String accessRole) {
        //future: enforce accessRole permissions in this method
        return dataStorage.getRecords(patientId, startTime, endTime);
    }
 
    /**
     * Retrieves most recent n records of a given type for a patient
     * Useful for trend analysis (e.g. "last 10 ECG readings").
     *
     * @param patientId unique identifier of patient
     * @param recordType type of vital to query (e.g. "ECG", "SystolicPressure")
     * @param n maximum number of records to return
     * @param accessRole role of requester -> future access-control checks
     * @return list of n most recent matching {@link PatientRecord} objects (sorted oldest-first) 
     *          may contain fewer than n entries if not enough records exist
     * @throws IllegalArgumentException if n is not positive
     */
    public List<PatientRecord> getLatestRecords(int patientId, String recordType, int n, String accessRole) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be a positive integer, got: " + n);
        }
 
        //retrieve all records across a wide time window, then filter by type
        List<PatientRecord> all = dataStorage.getRecords(patientId, 0L, Long.MAX_VALUE);
 
        List<PatientRecord> filtered = all.stream()
                                       .filter(r -> recordType.equals(r.getRecordType()))
                                       .collect(Collectors.toList());
 
        //return last n (most recent), preserving chronological order
        int fromIndex = Math.max(0, filtered.size() - n);
        return new ArrayList<>(filtered.subList(fromIndex, filtered.size()));
    }
 
    /**
     * Retrieves all records for all patients within a time window
     * Intended for admin-level or audit queries only
     *
     * @param startTime start of time window (in milliseconds since Unix epoch)
     * @param endTime end of the time window (in milliseconds since Unix epoch)
     * @param accessRole role of the requester
     * @return flat list of all {@link PatientRecord} objects across all patients that fall within the time range
     */
    public List<PatientRecord> getAllRecordsInRange(long startTime, long endTime, String accessRole) {
        List<PatientRecord> results = new ArrayList<>();
        for (Patient patient : dataStorage.getAllPatients()) {
            results.addAll(dataStorage.getRecords(patient.getPatientId(), startTime, endTime));
        }
        return results;
    }
 
    /**
     * Purges all records for a patient that are older than the specified cutoff timestamp
     * Supports data-deletion / retention policies (e.g. remove records older than 30 days)
     *
     * Actual deletion delegated to {@link DataStorage}
     *
     * @param patientId unique identifier of patient whose old records should be purged
     * @param cutoffTime records with a timestamp strictly before this value (milliseconds since Unix epoch) will be deleted
     * @param accessRole role of the requester -> only privileged roles should be permitted to delete data
     * @return number of records that were eligible for deletion 
     */
    public int purgeOldRecords(int patientId, long cutoffTime, String accessRole) {
        return dataStorage.deleteRecordsBefore(patientId, cutoffTime);
    }
}
 