package com.data_access;

import com.data_management.DataStorage;

/**
 * Bridges Data Access Layer and Data Storage subsystem
 * Single handoff point between two layers: 
    * Receives {@link ParsedRecord} objects from any {@link DataListener} implementation 
    * and forwards them to {@link DataStorage} 
 * 
 * Access layer is separated from storage layer 
    * Listeners (e.g. {@link DataListener}) & Parsers (e.g. {@link DataParser}) don't depend on {@link DataStorage}
    * Both know about this file 
 */
public class DataSourceAdapter {

    //storage instance that all parsed records are forwarded to
    private final DataStorage DATA_STORAGE;

    /**
     * Constructs a DataSourceAdapter that forwards records to given {@link DataStorage}
     *
     * @param DATA_STORAGE storage instance to write to (cannot be null)
     */
    public DataSourceAdapter(DataStorage dataStorage) {
        this.DATA_STORAGE = dataStorage;
    }

    /**
     * Forwards a successfully parsed record into data storage
     *
     * Called by a {@link DataListener} after {@link DataParser} has produced a valid {@link ParsedRecord}
     * Adapter translates record's fields into signature expected by {@link DataStorage#addPatientData}
     *
     * @param record parsed record to store (cannot be null)
     */
    public void store(ParsedRecord record) {
        DATA_STORAGE.addPatientData(
                record.getPatientId(),
                record.getMeasurementValue(),
                record.getLabel(),
                record.getTimestamp());
    }

    /**
     * Parses a raw message string and if parsing succeeds immediately forwards it to storage  
     * Malformed messages are logged and skipped without interrupting data stream
     *
     * Intended for use by TCP/WebSocket listeners: 
        * They receive data as raw strings and want a single call to handle both parsing and storing
     *
     * @param rawMessage raw string received from a data source
     * @param parser {@link DataParser} to use for parsing
     */
    public void parseAndStore(String rawMessage, DataParser parser) {
        try {
            ParsedRecord record = parser.parse(rawMessage);
            store(record);
        } 
        catch (DataParseException e) {
            System.err.println("[DataSourceAdapter] Skipping malformed message: " + e.getMessage());
        }
    }
}
