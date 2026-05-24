package com.app.server;


import com.app.repository.ClientRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class ClientHandler implements Runnable {

    private final Socket           socket;
    private final BlockingQueue<LogEntry> logQueue;
    private final ClientRepository clientRepository;
    private final Gson gson;

    private String clientId   = "UNKNOWN";
    private String hostname   = "UNKNOWN";
    private final String ipAddress;



    public ClientHandler(Socket socket, BlockingQueue<LogEntry> logQueue) {
        this.socket    = socket;
        this.logQueue  = logQueue;
        this.clientRepository = new ClientRepository();
        this.gson      = new Gson();
        this.ipAddress = socket.getInetAddress().getHostAddress();
    }




    @Override
    public void run() {
        System.out.println("[Server] New connection from: " + ipAddress);


        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                processMessage(line.trim());
            }

        } catch (IOException e) {
            System.err.println("[ClientHandler] Connection lost for "
                    + clientId + ": " + e.getMessage());
        } finally {
            // Always runs — even if an exception occurred
            onClientDisconnect();
        }
    }





    private void processMessage(String jsonLine) {
        try {
            JsonObject json = gson.fromJson(jsonLine, JsonObject.class);
            String type = json.get("type").getAsString();

            if ("REGISTER".equals(type)) {
                // Client is identifying itself on first connect
                clientId = json.get("clientId").getAsString();
                hostname = json.has("hostname")
                        ? json.get("hostname").getAsString() : "UNKNOWN";

                clientRepository.registerClient(clientId, hostname, ipAddress);
                System.out.println("[Server] Registered: " + clientId
                        + " (" + hostname + " @ " + ipAddress + ")");

            } else if ("LOG".equals(type)) {
                // Client is sending a log entry
                String level   = json.has("level")   ? json.get("level").getAsString()   : "INFO";
                String message = json.has("message") ? json.get("message").getAsString() : "";
                String file    = json.has("file")    ? json.get("file").getAsString()    : "";

                LogEntry entry = new LogEntry(clientId, level, message, file);


                logQueue.put(entry);
            }

        } catch (Exception e) {
            System.err.println("[ClientHandler] Bad message from "
                    + clientId + ": " + e.getMessage());
        }
    }



    private void onClientDisconnect() {
        System.out.println("[Server] Client disconnected: " + clientId);
        clientRepository.setClientStatus(clientId, "OFFLINE");
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error closing socket: " + e.getMessage());
        }
    }
}
