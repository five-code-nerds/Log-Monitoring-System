package com.app.alert;

public class AlertRule {

    private final String ruleName;
    private final String matchLevel;
    private final String keyword;
    private final String alertLevel;
    private final String description;




    public AlertRule(String ruleName, String matchLevel,
                     String keyword, String alertLevel, String description) {
        this.ruleName    = ruleName;
        this.matchLevel  = matchLevel;
        this.keyword     = keyword;
        this.alertLevel  = alertLevel;
        this.description = description;
    }



    public boolean matches(String logLevel, String message) {


        boolean levelMatch = (matchLevel == null)
                || matchLevel.equalsIgnoreCase(logLevel);



        boolean keywordMatch = (keyword == null)
                || message.toLowerCase().contains(keyword.toLowerCase());

        return levelMatch && keywordMatch;
    }

    // ── Getters ──
    public String getRuleName()    { return ruleName;    }
    public String getAlertLevel()  { return alertLevel;  }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("Rule[%s | level=%s | keyword=%s → %s]",
                ruleName, matchLevel, keyword, alertLevel);
    }
}
