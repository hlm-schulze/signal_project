package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {

    @Test
    void testAddAndGetRecords() {
        //We don't add a reader since the DataStorage constructor doesn't require it

        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }
       @Test
        public void testDuplicateRecordsAreNotAdded() {
            DataStorage storage = new DataStorage();

            storage.addPatientData(1, 80.0, "HeartRate", 1000);
            storage.addPatientData(1, 80.0, "HeartRate", 1000);

            List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);

            assertEquals(1, records.size());
        }
        @Test
        public void testConcurrentUpdates() throws InterruptedException {
            DataStorage storage = new DataStorage();

            Runnable task = () -> {
                for (int i = 0; i < 100; i++) {
                    storage.addPatientData(1, i, "HeartRate", i);
                }
            };

            Thread t1 = new Thread(task);
            Thread t2 = new Thread(task);

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);

            assertEquals(100, records.size());
        }
}