package com.app.server;


import com.app.alert.AlertEngine;
import com.app.repository.LogRepository;
import java.util.concurrent.BlockingQueue;




public class LogProcessor implements Runnable {

    private final BlockingQueue<LogEntry> logQueue;
    private final LogRepository logRepository;
    private final AlertEngine alertEngine;
    private volatile boolean  running = true;

    public LogProcessor(BlockingQueue<LogEntry> logQueue) {
        this.logQueue    = logQueue;
        this.logRepository  = new LogRepository();
        this.alertEngine = new AlertEngine();
    }

    @Override
    public void run() {
        System.out.println("[LogProcessor] Started — waiting for logs...");

        while (running) {
            try {


                LogEntry entry = logQueue.take();

                int logId = logRepository.insertLog(
                        entry.getClientId(),
                        entry.getLogLevel(),
                        entry.getMessage(),
                        entry.getSourceFile()
                );

                alertEngine.evaluate(entry, logId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[LogProcessor] Interrupted — shutting down.");
                break;
            }
        }

        System.out.println("[LogProcessor] Stopped.");
    }


    //stop() — shuts down this thread.

    public void stop() {
        running = false;
    }
}
