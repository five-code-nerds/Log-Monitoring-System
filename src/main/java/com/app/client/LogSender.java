package com.app.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;



public class LogSender {

    private final String serverHost;
    private final int    serverPort;
    private final String  clientId;
    private final Gson   gson;

    private Socket  socket;
    private PrintWriter writer;
    private boolean  connected = false;

    public LogSender(String serverHost, int serverPort, String clientId) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientId   = clientId;
        this.gson  = new Gson();
    }



    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);

            writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()), true
            );

            connected = true;
            System.out.println("[Client] Connected to server at "
                    + serverHost + ":" + serverPort);


            sendRegister();
            return true;

        } catch (IOException e) {
            System.err.println("[Client] Could not connect to server: "
                    + e.getMessage());
            connected = false;
            return false;
        }
    }



    private void sendRegister() {
        JsonObject json = new JsonObject();
        json.addProperty("type",     "REGISTER");
        json.addProperty("clientId", clientId);
        json.addProperty("hostname", getHostname());
        sendRaw(gson.toJson(json));
        System.out.println("[Client] Registered as: " + clientId);
    }

    /**
     * @param level      INFO / WARN / ERROR / CRITICAL
     * @param message    the log message text
     * @param sourceFile name of the file this log came from
     */
    public void sendLog(String level, String message, String sourceFile) {
        if (!connected) {
            System.err.println("[Client] Not connected — cannot send log.");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("type",    "LOG");
        json.addProperty("level",   level);
        json.addProperty("message", message);
        json.addProperty("file",    sourceFile);

        sendRaw(gson.toJson(json));
    }



    private void sendRaw(String json) {
        if (writer != null) {
            writer.println(json); // println = print + newline (\n)
        }
    }


    // disconnect() — closes the connection.

    public void disconnect() {
        connected = false;
        try {
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            System.out.println("[Client] Disconnected.");
        } catch (IOException e) {
            System.err.println("[Client] Error disconnecting: " + e.getMessage());
        }
    }


    // getHostname() — gets this machine's hostname for registration.

    private String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "UNKNOWN-HOST";
        }
    }

    public boolean isConnected() { return connected; }
}
