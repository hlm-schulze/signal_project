package com.data_access;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;

import java.io.IOException;

/**
 * Listens for patient data from log files and forwards it to {@link DataSourceAdapter} for storage
 *
 * Aadapts existing {@link FileDataReader} behind {@link DataListener} interface
 * Ensures that rest of the system can treat file-based input identically to TCP / WebSocket input 
 * Actual file reading and parsing logic stays in {@link FileDataReader}
 */
public class FileDataListener implements DataListener {

    //Path containing data files to read
    private final String BASE_DIRECTORY;

    //Storage instance that parsed records are written to
    private final DataStorage DATA_STORAGE;

    /**
     * Constructs a FileDataListener that reads from given directory and writes to given storage instance
     *
     * @param BASE_DIRECTORY path to directory containing data files
     * @param DATA_STORAGE storage instance to populate
     */
    public FileDataListener(String baseDirectory, DataStorage dataStorage) {
        this.BASE_DIRECTORY = baseDirectory;
        this.DATA_STORAGE = dataStorage;
    }

    /**
     * Reads all data files in configured directory and forwards each parsed record to {@link DataStorage}
     * Delegates entirely to {@link FileDataReader#readData(DataStorage)}
     *
     * @throws IOException if directory cannot be read / file cannot be opened
     */
    @Override
    public void startListening() throws IOException {
        FileDataReader reader = new FileDataReader(BASE_DIRECTORY);
        reader.readData(DATA_STORAGE);
        System.out.println("[FileDataListener] Finished reading from: " + BASE_DIRECTORY);
    }

    /**
     * Doesn't do anything for file-based input
     * Included for compliance with {@link DataListener} interface
     */
    @Override
    public void stopListening() {
        //File reading is one-shot -> nothing to stop
    }

    /**
     * Returns a short description of current listener's source for logging
     *
     * @return string identifying base directory
     */
    @Override
    public String getSourceDescription() {
        return "File:" + BASE_DIRECTORY;
    }
}
