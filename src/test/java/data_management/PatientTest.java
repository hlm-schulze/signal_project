package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Unit tests for {@link Patient}
 *
 * Covers addRecord, getRecords (filtering to get within a time range), and deleteRecordsBefore (retention policy)
 */
class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(64);
    }

    //getPatientId
    @Test
    void testGetPatientId() {
        assertEquals(64, patient.getPatientId());
    }

    //addRecord / getRecords
    @Test
    void testAddAndRetrieveSingleRecord() {
        patient.addRecord(75.0, "HeartRate", 1000L);
        List<PatientRecord> records = patient.getRecords(500L, 1500L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(64, records.get(0).getPatientId());
    }

    @Test
    void testGetRecordsMultipleWithinRange() {
        patient.addRecord(1.0, "ECG", 1000L);
        patient.addRecord(2.0, "ECG", 2000L);
        patient.addRecord(3.0, "ECG", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 3000L);
        assertEquals(3, records.size());
    }

//getRecords -> boundary conditions
    @Test
    void testGetRecordsIncludesStartBoundary() {
        patient.addRecord(10.0, "ECG", 1000L);
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(1, records.size(), "Record exactly at startTime should be included");
    }

    @Test
    void testGetRecordsIncludesEndBoundary() {
        patient.addRecord(10.0, "ECG", 2000L);
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(1, records.size(), "Record exactly at endTime should be included");
    }

    @Test
    void testGetRecordsExcludesBeforeStart() {
        patient.addRecord(10.0, "ECG", 500L);
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(0, records.size(), "Record before startTime should be excluded");
    }

    @Test
    void testGetRecordsExcludesAfterEnd() {
        patient.addRecord(10.0, "ECG", 3000L);
        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(0, records.size(), "Record after endTime should be excluded");
    }

    @Test
    void testGetRecordsEmptyWhenNoRecordsAdded() {
        List<PatientRecord> records = patient.getRecords(0L, Long.MAX_VALUE);
        assertNotNull(records);
        assertEquals(0, records.size());
    }

    @Test
    void testGetRecordsFiltersCorrectlyAmongMixedTimestamps() {
        patient.addRecord(1.0, "ECG", 500L);   //outside (too early)
        patient.addRecord(2.0, "ECG", 1000L);  //inside
        patient.addRecord(3.0, "ECG", 1500L);  //inside
        patient.addRecord(4.0, "ECG", 2500L);  //outside (too late)

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
        assertEquals(2.0, records.get(0).getMeasurementValue());
        assertEquals(3.0, records.get(1).getMeasurementValue());
    }


//deleteRecordsBefore
    @Test
    void testDeleteRecordsBeforeRemovesOldRecords() {
        patient.addRecord(1.0, "ECG", 1000L);
        patient.addRecord(2.0, "ECG", 2000L);
        patient.addRecord(3.0, "ECG", 3000L);

        int deleted = patient.deleteRecordsBefore(2000L);

        assertEquals(1, deleted, "One record (ts=1000) should have been deleted");
        List<PatientRecord> remaining = patient.getRecords(0L, Long.MAX_VALUE);
        assertEquals(2, remaining.size());
    }

    @Test
    void testDeleteRecordsBeforeKeepsRecordsAtCutoff() {
        patient.addRecord(1.0, "ECG", 2000L); //exactly at cutoff -> should be kept
        int deleted = patient.deleteRecordsBefore(2000L);

        assertEquals(0, deleted, "Record at cutoff timestamp should not be deleted");
    }

    @Test
    void testDeleteRecordsBeforeAllRecords() {
        patient.addRecord(1.0, "ECG", 100L);
        patient.addRecord(2.0, "ECG", 200L);

        int deleted = patient.deleteRecordsBefore(Long.MAX_VALUE);

        assertEquals(2, deleted);
        assertEquals(0, patient.getRecords(0L, Long.MAX_VALUE).size());
    }

    @Test
    void testDeleteRecordsBeforeNoMatchingRecords() {
        patient.addRecord(1.0, "ECG", 5000L);
        int deleted = patient.deleteRecordsBefore(1000L);

        assertEquals(0, deleted, "No records should be deleted when all are after cutoff");
        assertEquals(1, patient.getRecords(0L, Long.MAX_VALUE).size());
    }
}