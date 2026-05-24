package com.app.server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class LogServer {

    private static final int PORT = 9090;
    private static final int THREAD_POOL = 10;


    private final BlockingQueue<LogEntry> logQueue =
            new LinkedBlockingQueue<>(500);

    private final ExecutorService executor =
            Executors.newFixedThreadPool(THREAD_POOL);

    private ServerSocket serverSocket;
    private volatile boolean running = true;


    //start() — begins listening for client connections and also starts the LogProcessor background thread.

    public void start() {


        Thread processorThread = new Thread(new LogProcessor(logQueue));
        processorThread.setDaemon(true);
        processorThread.start();

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[Server] Listening on port " + PORT + "...");

            // Main accept loop — runs until server is stopped
            while (running) {

                Socket clientSocket = serverSocket.accept();


                executor.submit(new ClientHandler(clientSocket, logQueue));
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[Server] Error: " + e.getMessage());
            }
        }
    }


    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[Server] Error stopping: " + e.getMessage());
        }

        System.out.println("[Server] Stopped.");
    }

    public static void main(String[] args) {
        LogServer server = new LogServer();
        server.start();
    }
}
