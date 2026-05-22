package com.data_access;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.net.URI; //like URL but only shows what something is not how to access it 

/**
 * Listens for incoming patient data over a WebSocket connection and forwards each message to {@link DataSourceAdapter} for parsing and storage
 *
 * Receiving counterpart of {@link WebSocketOutputStrategy}: 
    * Simulator broadcasts data to connected WebSocket clients, 
    * and this listener acts as one of those clients on CHMS side
 */
public class WebSocketDataListener implements DataListener {

    //WebSocket server URI to connect to (e.g. {@code ws://localhost:8080})
    private final String SERVER_URI;

    //Hands parsed records to storage
    private final DataSourceAdapter ADAPTER;

    //Used to convert raw WebSocket messages into ParsedRecords
    private final DataParser PARSER;

    //Underlying WebSocket client connection
    private WebSocketClient client;

    /**
     * Constructs {@code WebSocketDataListener} that will connect to given WebSocket server URI
     *
     * @param SERVER_URI URI of the WebSocket server (e.g. "ws://localhost:8080")
     * @param ADAPTER adapter to forward parsed records to
     * @param PARSER parser to use for incoming messages
     */
    public WebSocketDataListener(String serverURI, DataSourceAdapter adapter, DataParser parser) {
        this.SERVER_URI = serverURI;
        this.ADAPTER = adapter;
        this.PARSER = parser;
    }

    /**
     * Opens a WebSocket connection to configured server URI and begins receiving messages
     * Each incoming message is forwarded to {@link DataSourceAdapter#parseAndStore}
        * -> handles parsing and storage, silently skipping malformed messages.
     *
     * Blocks until connection is established
     * Messages are then delivered asynchronously via client's onMessage callback
     * 
     * (similar pattern in {@link WebSocketOutputStrategy}
     * 
     * @throws Exception if URI is malformed / connection cannot be established
     */
    @Override
    public void startListening() throws Exception {
        client = new WebSocketClient(new URI(SERVER_URI)) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("[WebSocketDataListener] Connected to " + SERVER_URI);
            }

            @Override
            public void onMessage(String message) {
                    try {
                        ADAPTER.parseAndStore(message, PARSER);
                    } catch (Exception e) {
                        System.err.println("[WebSocketDataListener] Failed to process message: " + message);
                        System.err.println(e.getMessage());
                    }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("[WebSocketDataListener] Connection closed: " + reason);

                try {
                    Thread.sleep(3000);
                    System.out.println("[WebSocketDataListener] Attempting reconnection...");
                    client.reconnect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("[WebSocketDataListener] Error: " + ex.getMessage());
            }
        };

        client.connectBlocking();
    }

    //Closes the WebSocket connection, stopping message delivery
    @Override
    public void stopListening() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Returns short description of this listener's source for logging
     *
     * @return string identifying WebSocket server URI
     */
    @Override
    public String getSourceDescription() {
        return "WebSocket:" + SERVER_URI;
    }
}
