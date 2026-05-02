package com.data_access;

/**
 * Parses raw string messages from any data source into standardized {@link ParsedRecord} objects 
 * (rest of system can work with this record)
 * 
 * Currently HealthDataSimulator produces data in two formats (depending on output strategy)
    * single CSV format (in TCP & WebSocket output strategies):
        * "patientId,timestamp,label,data" (used by TCP and WebSocket output)
    * and a labeled format (in file output strategy):
        * "Patient ID: 1, Timestamp: 123, Label: ECG, Data: 0.45"
 *
 * Both formats are handled in this class
    * Keeps all parsing logic in one place rather than duplicated across each listener
 *
 * If a message does not match any known format: 
    * {@link DataParseException} is thrown so the caller can log and skip malformed record without crashing listener
 */
public class DataParser {

    /**
     * Attempts to parse a raw message string into a {@link ParsedRecord}
     * Tries compact CSV format first, then labeled format
     *
     * @param rawMessage raw string received from a data source (cannot be null)
     * @return {@link ParsedRecord} populated with extracted fields
     * @throws DataParseException if message does not match any known format / contains invalid field values
     */
    public ParsedRecord parse(String rawMessage) throws DataParseException {
        if (rawMessage == null) {
            throw new DataParseException("Raw message must not be null");
        }

        String trimmed = rawMessage.trim();

        //try compact CSV format first
        if (!trimmed.startsWith("Patient")) {
            return parseCsv(trimmed);
        }

        //if it does not work try labeled format
        return parseLabeled(trimmed);
    }

    /**
     * Parses compact CSV format produced by TCP & WebSocket output strategies
     *
     * @param raw trimmed raw message string
     * @return populated {@link ParsedRecord}
     * @throws DataParseException if format is invalid / fields cannot be converted to their expected types
     */
    private ParsedRecord parseCsv(String raw) throws DataParseException {
        String[] parts = raw.split(",", 4);

        if (parts.length != 4) {
            throw new DataParseException("CSV format expects 4 fields, got " + parts.length + ": " + raw);
        }

        try {
            int patientId = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label = parts[2].trim();
            double value = Double.parseDouble(parts[3].trim().replace("%", ""));

            return new ParsedRecord(patientId, timestamp, label, value);
        } 
        catch (NumberFormatException e) {
            throw new DataParseException("Invalid numeric field in CSV message: " + raw, e);
        }
    }

    /**
     * Parses labeled format produced by file output strategy
     *
     * @param raw trimmed raw message string
     * @return populated {@link ParsedRecord}
     * @throws DataParseException if format is invalid / fields cannot be converted to their expected types
     */
    private ParsedRecord parseLabeled(String raw) throws DataParseException {
        String[] parts = raw.split(", ", 4);

        if (parts.length != 4) {
            throw new DataParseException("Labeled format expects 4 key-value pairs, got " + parts.length + ": " + raw);
        }

        try {
            int patientId = Integer.parseInt(parts[0].replace("Patient ID:", "").trim());
            long timestamp = Long.parseLong(parts[1].replace("Timestamp:", "").trim());
            String label = parts[2].replace("Label:", "").trim();
            double value = Double.parseDouble(parts[3].replace("Data:", "").trim().replace("%", ""));

            return new ParsedRecord(patientId, timestamp, label, value);
        } 
        catch (NumberFormatException e) {
            throw new DataParseException("Invalid numeric field in labeled message: " + raw, e);
        }
    }
}
