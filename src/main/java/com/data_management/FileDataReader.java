package com.data_management;
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
 
/**
 * Implementation of {@link DataReader} that reads patient data from text files in a specified directory
 * Each file corresponds to a data label (e.g. "ECG.txt", "SystolicPressure.txt") 
 * and was written by {@link com.cardio_generator.outputs.FileOutputStrategy}
 */
public class FileDataReader implements DataReader {
 
    private final String baseDirectory;
 
    /**
     * Constructs a new {@code FileDataReader} that reads from specified directory
     *
     * @param baseDirectory the path to the directory containing data files
     */
    public FileDataReader(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
 
    /**
     * Reads all {@code .txt} files in base directory and stores each parsed record into provided {@link DataStorage}
     *
     * @param dataStorage the storage where parsed records will be stored
     * @throws IOException if the directory cannot be read or a file cannot be opened
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path directory = Paths.get(baseDirectory);
 
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            throw new IOException("Directory not found or is not a directory: " + baseDirectory);
        }
 
        // Walk through all .txt files in the directory
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> 
                p.toString().endsWith(".txt")).forEach(filePath -> {
                    try {
                        parseFile(filePath, dataStorage);
                    } 
                    catch (IOException e) {
                        System.err.println("Error reading file " + filePath + ": " + e.getMessage());
                    }
                 });
        }
    }
 
    /**
     * Parses single data file and adds each valid record to data storage
     * Lines that do not match expected format are skipped with a warning
     *
     * @param filePath path to file to parse
     * @param dataStorage storage to populate
     * @throws IOException if file cannot be read
     */
    private void parseFile(Path filePath, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line = reader.readLine();
            while (line != null) {
                parseLine(line, dataStorage);
                 line = reader.readLine();
            }
        }
    }
 
    /**
     * Parses a single line in format:
     * {@code Patient ID: 1, Timestamp: 1234567890000, Label: ECG, Data: 0.45}
     * & adds record to data storage
     *
     * Lines that do not match expected format/contain invalid values are skipped with a warning message
     *
     * @param line raw line string to parse
     * @param dataStorage storage to populate
     */
    private void parseLine(String line, DataStorage dataStorage) {
        try {
            // Split on ", " to get four key-value pairs
            String[] parts = line.split(", ");
            if (parts.length != 4) {
                System.err.println("Skipping malformed line: " + line);
                return;
            }
 
            int patientId = Integer.parseInt(parts[0].replace("Patient ID: ", "").trim());
            long timestamp = Long.parseLong(parts[1].replace("Timestamp: ", "").trim());
            String label = parts[2].replace("Label: ", "").trim();
            String rawData = parts[3].replace("Data: ", "").trim();
 
            // Strip any trailing % (e.g. from blood saturation values like "98.0%")
            double measurementValue = Double.parseDouble(rawData.replace("%", ""));
 
            dataStorage.addPatientData(patientId, measurementValue, label, timestamp);
 
        } catch (NumberFormatException e) {
            System.err.println("Skipping line with invalid number format: " + line);
        }
    }
}
 