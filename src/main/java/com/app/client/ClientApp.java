package com.app.client;

public class ClientApp {

    // ── Configuration ─--
    private static final String SERVER_HOST = "localhost"; // server IP
    private static final int    SERVER_PORT = 9090;

    // Change these per client instance to simulate multiple clients:
    private static final String CLIENT_ID   = "CLIENT-01";
    private static final String LOG_FILE    = "Logs/sample.log";
    // ────────────────

    public static void main(String[] args) {

        // Allow overriding via command-line arguments:
        // java -jar client.jar CLIENT-02 logs/app2.log
        String clientId = args.length > 0 ? args[0] : CLIENT_ID;
        String logFile  = args.length > 1 ? args[1] : LOG_FILE;

        System.out.println("[Client] Starting client: " + clientId);
        System.out.println("[Client] Monitoring file: " + logFile);

        // 1. Create the sender (manages socket connection)
        LogSender sender = new LogSender(SERVER_HOST, SERVER_PORT, clientId);

        // 2. Connect to server
        if (!sender.connect()) {
            System.err.println("[Client] Failed to connect. Is the server running?");
            System.exit(1);
        }

        // 3. Create and start the file reader in its own thread
        LogFileReader reader = new LogFileReader(logFile, sender);
        Thread readerThread  = new Thread(reader);
        readerThread.setDaemon(true);
        readerThread.start();

        // 4. Keep running until Ctrl+C is pressed
        // Add a shutdown hook to cleanly disconnect
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Client] Shutting down...");
            reader.stop();
            sender.disconnect();
        }));

        // Block main thread so the program keeps running
        try {
            readerThread.join(); // wait for reader thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
