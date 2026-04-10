package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/*
 * Implementation of OutputStrategy that sends patient data over TCP
 * Output strategy for sending patient data over TCP
 * Starts a TCP server on the specified port & waits for a client to connect
 * Once a client is connected -> sends the generated patient data in the format: "Patient ID, Timestamp, Label, Data"
 * Server runs in a separate thread to allow main thread to continue generating patient data without blocking
 */

public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

   /**
    * Constructs new TcpOutputStrategy and starts a TCP server on the specified port
    * Server accepts a single client connection and sends generated patient data to the connected client
    * 
    * @param port port number on which the TCP server will listen for incoming client connections
    */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the generated patient data to the connected client
     * If no client is connected, the method will simply return without sending data (silently skipping)
     * 
     * @param patientId  ID of the patient for who the data was generated
     * @param timestamp  time when the data was generated
     * @param label  label describing the type of data being output (e.g., "ECG", "Blood Pressure")
     * @param data  actual generated data to be outputted
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
