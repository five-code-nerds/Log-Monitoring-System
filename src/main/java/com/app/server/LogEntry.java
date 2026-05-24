package com.app.server;

public class LogEntry {

    private final String clientId;
    private final String logLevel;
    private final String message;
    private final String sourceFile;
    private final long   timestamp;

    public LogEntry(String clientId, String logLevel,
                    String message, String sourceFile) {
        this.clientId   = clientId;
        this.logLevel   = logLevel;
        this.message    = message;
        this.sourceFile = sourceFile;
        this.timestamp  = System.currentTimeMillis();
    }

    // ── Getters  ----
    public String getClientId()   { return clientId;   }
    public String getLogLevel()   { return logLevel;   }
    public String getMessage()    { return message;    }
    public String getSourceFile() { return sourceFile; }



    @Override
    public String toString() {
        return String.format("[%s] %s: %s", logLevel, clientId, message);
    }
}
