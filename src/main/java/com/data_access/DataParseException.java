package com.data_access;

/**
 * Thrown by {@link DataParser} when a raw message string cannot be parsed into a valid {@link ParsedRecord}
 * Checked exception: All callers of {@link DataParser#parse(String)} are forced to explicitly decide what to do with a malformed message 
 * (e.g. logging and skipping it) rather than letting a silent failure corrupt data pipeline
 */
public class DataParseException extends Exception {

    /**
     * Constructs a DataParseException with given detail message
     * @param message description of why parsing failed
     */
    public DataParseException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code DataParseException} with a detail message and underlying cause 
     * (e.g. NumberFormatException from a bad field)
     *
     * @param message description of why parsing failed
     * @param cause underlying exception that triggered failure
     */
    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
