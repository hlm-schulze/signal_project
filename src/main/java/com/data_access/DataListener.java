package com.data_access;

/**
 * Interface -> defines contract for all data source listeners in Data Access Layer
 * Any class that receives incoming patient data must implement this interface(e.g. TCPDataListener)
 * Keeps rest of system (storage, alerts) completely unaware of how data arrives
 */
public interface DataListener {

    /**
     * Starts listening for incoming data from this listener's source
     *
     * @throws Exception if connection cannot be established / IO error occurs while reading
     */
    void startListening() throws Exception;

    /**
     * Stops listening and releases any resources held by current listener 
     * Should be able to call this method even if startListening() hasn't been called 
     */
    void stopListening();

    /**
     * Returns a short, human-readable description of data source current listener is connected to
     * Used for logging & diagnostics
     *
     * @return source description string (e.g. "TCP:localhost:8080")
     */
    String getSourceDescription();
}
