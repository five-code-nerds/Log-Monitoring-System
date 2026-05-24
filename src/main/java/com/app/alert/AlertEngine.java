package com.app.alert;


import com.app.repository.AlertRepository;
import com.app.server.LogEntry;

import java.util.ArrayList;
import java.util.List;




public class AlertEngine {

    private final List<AlertRule> rules;
    private final AlertRepository alertRepository;

    public AlertEngine() {
        this.alertRepository = new AlertRepository();
        this.rules = new ArrayList<>();
        loadDefaultRules();
    }

    /**
     * loadDefaultRules() — defines all alert rules.
     * <p>
     * Rule structure: (name, matchLevel, keyword, alertLevel, description)
     */
    private void loadDefaultRules() {


        rules.add(new AlertRule(
                "Critical Level Rule",
                "CRITICAL",
                null,                // any message
                "CRITICAL",
                "A CRITICAL log entry was received — immediate attention required."
        ));

        // Rule 2: Any ERROR log → fire an ERROR alert
        rules.add(new AlertRule(
                "Error Level Rule",
                "ERROR",
                null,
                "ERROR",
                "An ERROR was detected in the system."
        ));


        rules.add(new AlertRule(
                "Disk Keyword Rule",
                null,                // any level
                "disk",
                "WARN",
                "Disk-related issue detected in log message."
        ));

        // Rule 4: Any log mentioning "failed" → ERROR alert
        rules.add(new AlertRule(
                "Failed Keyword Rule",
                null,
                "failed",
                "ERROR",
                "A failure was detected in the log message."
        ));

        // Rule 5: Any log mentioning "memory" → WARN alert
        rules.add(new AlertRule(
                "Memory Keyword Rule",
                null,
                "memory",
                "WARN",
                "Memory-related issue detected."
        ));

        // Rule 6: Any log mentioning "exception" → ERROR alert
        rules.add(new AlertRule(
                "Exception Keyword Rule",
                null,
                "exception",
                "ERROR",
                "An exception was logged — check stack trace."
        ));

        System.out.println("[AlertEngine] Loaded " + rules.size() + " rules.");
    }

    /**
     * @param entry the incoming log entry
     * @param logId the DB-generated ID of the saved log (for linking)
     */
    public void evaluate(LogEntry entry, int logId) {

        for (AlertRule rule : rules) {

            if (rule.matches(entry.getLogLevel(), entry.getMessage())) {

                // Build a descriptive alert message
                String alertMsg = String.format(
                        "[%s] %s | Client: %s | Log: %s",
                        rule.getRuleName(),
                        rule.getDescription(),
                        entry.getClientId(),
                        entry.getMessage()
                );

                // Save alert to database
                alertRepository.insertAlert(
                        entry.getClientId(),
                        logId,
                        rule.getAlertLevel(),
                        alertMsg
                );

                System.out.println("[AlertEngine] ALERT fired → "
                        + rule.getAlertLevel()
                        + " | Rule: " + rule.getRuleName()
                        + " | Client: " + entry.getClientId());
            }
        }
    }

    /**
     * addRule() — allows adding a new rule at runtime.
     * Useful if we later add a GUI panel to manage rules.
     */
    public void addRule(AlertRule rule) {
        rules.add(rule);
        System.out.println("[AlertEngine] New rule added: " + rule);
    }

    /**
     * getRules() — returns all current rules (for display in GUI).
     */
    public List<AlertRule> getRules() {
        return new ArrayList<>(rules); // return a copy, not the original list
    }
}

