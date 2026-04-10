package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Implementation of OutputStrategy that writes patient data to files
 * Output strategy for writing patient data to files
 * Each label corresponds to a separate file (e.g., "ECG.txt", "Blood Pressure.txt")
 * Files are created in the specified base directory if they do not exist
 */

public class FileOutputStrategy implements OutputStrategy {

    private String baseDirectory; //changed to camelCase (all the following instances as well): BaseDirectory -> baseDirectory

    //removed underscore and changed to camelCase (all the following instances as well): file_map -> fileMap
    /**
     * Maps each data label to its corresponding output file path
     * Uses a ConcurrentHashMap to ensure thread-safe access across multiple patient data generators running in parallel
     */
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>(); 

    /**
     * Constructs new FileOutputStrategy with the specified base directory
     *
     * @param baseDirectory  directory where output files will be created
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes patient data to the appropriate file based on the label
     * If a file for the label does not exist, it is created automatically in the base directory
     * Each entry in file has the format: "Patient ID: {patientId}, Timestamp: {timestamp}, Label: {label}, Data: {data}"
     * 
     * @param patientId  ID of the patient for who the data was generated
     * @param timestamp  time when the data was generated
     * @param label  label describing the type of data being output (e.g., "ECG", "Blood Pressure")
     * @param data  actual generated data to be outputted
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the filePath variable
        //changed to camelCase (all the following instances as well): FilePath -> filePath
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString()); 

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}