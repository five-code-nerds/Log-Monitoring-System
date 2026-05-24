package com.app.client;

import java.io.*;
import java.nio.file.*;




public class LogFileReader implements Runnable {

    private final String     filePath;
    private final LogSender  sender;
    private volatile boolean running = true;

    // How often to check for new lines (milliseconds)
    private static final int POLL_INTERVAL_MS = 500;

    public LogFileReader(String filePath, LogSender sender) {
        this.filePath = filePath;
        this.sender   = sender;
    }

    @Override
    public void run() {
        File file = new File(filePath);

        // Wait until the file actually exists before starting
        while (running && !file.exists()) {
            System.out.println("[FileReader] Waiting for file: " + filePath);
            sleep(2000);
        }

        System.out.println("[FileReader] Tailing file: " + filePath);

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            // Seek to END of file on startup — we only want NEW lines
            // file.length() returns the total byte size of the file
            long filePointer = file.length();
            raf.seek(filePointer);

            // Main polling loop
            while (running) {
                long currentLength = file.length();

                if (currentLength < filePointer) {
                    // File was rotated/truncated — start from beginning
                    System.out.println("[FileReader] File rotated, resetting.");
                    filePointer = 0;
                    raf.seek(0);

                } else if (currentLength > filePointer) {
                    // New content has been written — read it
                    String line;

                    // readLine() reads one line and advances the
                    // internal pointer. Returns null at end of file.
                    while ((line = raf.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            processLine(line.trim());
                        }
                    }

                    // Save current position for next poll cycle
                    filePointer = raf.getFilePointer();
                }

                // Wait before checking again
                sleep(POLL_INTERVAL_MS);
            }

        } catch (IOException e) {
            System.err.println("[FileReader] Error reading file: " + e.getMessage());
        }

        System.out.println("[FileReader] Stopped tailing: " + filePath);
    }



    private void processLine(String line) {
        String level   = "INFO"; // default level
        String message = line;

        // Check if line starts with a known log level prefix
        String upper = line.toUpperCase();
        if (upper.startsWith("CRITICAL:") || upper.startsWith("[CRITICAL]")) {
            level   = "CRITICAL";
            message = stripPrefix(line);
        } else if (upper.startsWith("ERROR:") || upper.startsWith("[ERROR]")) {
            level   = "ERROR";
            message = stripPrefix(line);
        } else if (upper.startsWith("WARN:") || upper.startsWith("[WARN]")) {
            level   = "WARN";
            message = stripPrefix(line);
        } else if (upper.startsWith("INFO:") || upper.startsWith("[INFO]")) {
            level   = "INFO";
            message = stripPrefix(line);
        }

        // Get just the filename from the full path
        String fileName = Paths.get(filePath).getFileName().toString();

        sender.sendLog(level, message, fileName);
        System.out.println("[FileReader] Sent → " + level + ": " + message);
    }


    // stripPrefix() — removes "ERROR:" or "[ERROR]" from the start of a line.

    private String stripPrefix(String line) {
        // Remove everything up to and including the first ':' or ']'
        int colonIdx  = line.indexOf(':');
        int bracketIdx = line.indexOf(']');

        if (colonIdx > 0 && (bracketIdx < 0 || colonIdx < bracketIdx)) {
            return line.substring(colonIdx + 1).trim();
        } else if (bracketIdx > 0) {
            return line.substring(bracketIdx + 1).trim();
        }
        return line;
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() { running = false; }
}
