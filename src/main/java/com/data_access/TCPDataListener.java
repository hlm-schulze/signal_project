package com.data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.cardio_generator.outputs.TcpOutputStrategy;

/**
 * Listens for incoming patient data over a TCP connection and forwards each message to {@link DataSourceAdapter} for parsing and storage
 *
 * Receiving counterpart of {@link TcpOutputStrategy}: 
    * Simulator sends data over TCP, and this listener reads it line by line on CHMS side
 * Two sides are completely separated 
    * -> this class only knows it is reading from a source, not that a specific simulator produced data
 *
 * Implements {@link DataListener} so that rest of system can treat all listener types uniformly via shared interface
 */
public class TCPDataListener implements DataListener {

    //Hostname/IP address of TCP server to connect to
    private final String HOST;

    //Port number of TCP server
    private final int PORT;

    //Hands parsed records to storage
    private final DataSourceAdapter ADAPTER;

    //Used to convert raw TCP messages into ParsedRecords
    private final DataParser PARSER;

    //Used to stop listening loop
    private boolean running;

    /**
     * Constructs TCPDataListener that will connect to given host and port, parsing and forwarding all received messages
     *
     * @param HOST hostname/IP of TCP server (simulator)
     * @param PORT port number simulator is broadcasting on
     * @param ADAPTER adapter to forward parsed records to
     * @param PARSER parser to use for incoming messages
     */
    public TCPDataListener(String host, int port, DataSourceAdapter adapter, DataParser parser) {
        this.HOST = host;
        this.PORT = port;
        this.ADAPTER = adapter;
        this.PARSER = parser;
    }

    /**
     * Opens a TCP connection to configured host and port, then reads incoming messages line by line 
     * until stopListening() is called / connection is closed by remote end
     *
     * Each line is passed to {@link DataSourceAdapter#parseAndStore}
        * -> handles both parsing and storage, and silently skips any malformed messages
     *
     * @throws IOException if connection cannot be established or is interrupted unexpectedly
     */
    @Override
    public void startListening() throws IOException {
        running = true;

        try (Socket socket = new Socket(HOST, PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) 
            {
            System.out.println("[TCPDataListener] Connected to " + HOST + ":" + PORT);
            String line;

            while (running && (line = reader.readLine()) != null) {
                ADAPTER.parseAndStore(line, PARSER);
            }
        }

        System.out.println("[TCPDataListener] Stopped.");
    }

    /**
     * Signals listening loop to stop after current read completes
     * Connection will be closed gracefully on next iteration
     */
    @Override
    public void stopListening() {
        running = false;
    }

    /**
     * Returns a short description of this listener's source for logging
     *
     * @return a string identifying TCP host and port
     */
    @Override
    public String getSourceDescription() {
        return "TCP:" + HOST + ":" + PORT;
    }
}
