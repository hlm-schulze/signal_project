package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

/**
 * Unit tests for {@link FileDataReader}
 *
 * Uses JUnit's @TempDir to create a real temporary directory for each test so no mock testing is required
 * Each test writes a small .txt file in format produced by {@link com.cardio_generator.outputs.FileOutputStrategy}
 * and then checks that records are parsed and stored correctly
 */
class FileDataReaderTest {

    @TempDir
    Path tempDir;

    private DataStorage storage;
    private FileDataReader reader;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        reader  = new FileDataReader(tempDir.toString());
    }


//Write a single data file into temp directory
    private void writeFile(String filename, String... lines) throws IOException {
        Path file = tempDir.resolve(filename);
        try (PrintWriter pw = new PrintWriter(file.toFile())) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }


//Normal parsing
    @Test
    void testReadSingleRecord() throws IOException {
        writeFile("ECG.txt", 
                "Patient ID: 1, Timestamp: 1714376789050, Label: ECG, Data: 0.45");

        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789050L);
        assertEquals(1, records.size());
        assertEquals(0.45, records.get(0).getMeasurementValue(), 0.001);
        assertEquals("ECG", records.get(0).getRecordType());
    }

    @Test
    void testReadMultipleRecordsSamePatient() throws IOException {
        writeFile("HeartRate.txt",
                "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0",
                "Patient ID: 1, Timestamp: 2000, Label: HeartRate, Data: 75.0",
                "Patient ID: 1, Timestamp: 3000, Label: HeartRate, Data: 78.0");

        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 3000L);
        assertEquals(3, records.size());
    }

    @Test
    void testReadRecordsDifferentPatients() throws IOException {
        writeFile("SystolicPressure.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0",
                "Patient ID: 2, Timestamp: 1000, Label: SystolicPressure, Data: 130.0");

        reader.readData(storage);

        assertEquals(1, storage.getRecords(1, 1000L, 1000L).size());
        assertEquals(1, storage.getRecords(2, 1000L, 1000L).size());
    }

    @Test
    void testReadMultipleFiles() throws IOException {
        writeFile("ECG.txt",
                "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.3");
        writeFile("Saturation.txt",
                "Patient ID: 1, Timestamp: 2000, Label: Saturation, Data: 97.0");

        reader.readData(storage);

        // Both record types should be stored
        List<PatientRecord> all = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(2, all.size());
    }

    @Test
    void testSaturationPercentSignIsStripped() throws IOException {
        //FileOutputStrategy appends '%' to saturation values
        writeFile("Saturation.txt",
                "Patient ID: 1, Timestamp: 1000, Label: Saturation, Data: 95.0%");

        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(95.0, records.get(0).getMeasurementValue(), 0.001);
    }


//Edge cases
    @Test
    void testEmptyFileProducesNoRecords() throws IOException {
        writeFile("Empty.txt"); //no lines

        reader.readData(storage);

        assertEquals(0, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testMalformedLineIsSkipped() throws IOException {
        writeFile("Bad.txt", "this line is completely wrong",
                "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.5");

        //Should not throw -> bad line is skipped , good line is parsed
        assertDoesNotThrow(() -> reader.readData(storage));
        assertEquals(1, storage.getRecords(1, 1000L, 1000L).size());
    }

    @Test
    void testNonTxtFilesAreIgnored() throws IOException {
        //.csv file should not be read
        Path csvFile = tempDir.resolve("data.csv");
        try (PrintWriter pw = new PrintWriter(csvFile.toFile())) {
            pw.println("Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.5");
        }

        reader.readData(storage);

        assertEquals(0, storage.getRecords(1, 0L, Long.MAX_VALUE).size(),
                "Non-.txt files should be ignored");
    }

    @Test
    void testDirectoryNotFoundThrowsIOException() {
        FileDataReader badReader = new FileDataReader("/nonexistent/path/xyz");
        assertThrows(IOException.class, () -> badReader.readData(storage));
    }

    @Test
    void testInvalidNumberInLineIsSkipped() throws IOException {
        writeFile("ECG.txt",
                "Patient ID: abc, Timestamp: 1000, Label: ECG, Data: 0.5");

        assertDoesNotThrow(() -> reader.readData(storage));
        assertEquals(0, storage.getRecords(0, 0L, Long.MAX_VALUE).size(),
                "Line with invalid patient ID should be skipped");
    }
}