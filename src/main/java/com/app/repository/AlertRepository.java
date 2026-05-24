package com.app.repository;


import com.app.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AlertRepository {

    /*
      lRemainder:logId links this alert to the specific log that triggered it.
     */
    public void insertAlert(String clientId, int logId,
                            String alertLevel, String message) {
        final String sql = """
                INSERT INTO alerts (client_id, log_id, alert_level, message)
                VALUES (?, ?, ?, ?)
                """;

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, clientId);
            // logId of -1 means we don't have a linked log — use NULL
            if (logId > 0) stmt.setInt(2, logId);
            else           stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, alertLevel);
            stmt.setString(4, message);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[Alert] Error inserting alert: " + e.getMessage());
        }
    }

    /*
     getActiveAlerts — returns all unresolved alerts.
     is_resolved = 0 means the alert is still active.
     */
    public List<String[]> getActiveAlerts() {
        List<String[]> alerts = new ArrayList<>();
        final String sql = """
                SELECT id, client_id, alert_level, message, triggered_at
                FROM alerts
                WHERE is_resolved = 0
                ORDER BY triggered_at DESC
                """;

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                alerts.add(new String[]{
                        rs.getString("id"),
                        rs.getString("client_id"),
                        rs.getString("alert_level"),
                        rs.getString("message"),
                        rs.getString("triggered_at")
                });
            }

        } catch (SQLException e) {
            System.err.println("[Alert] Error fetching alerts: " + e.getMessage());
        }

        return alerts;
    }

    /*
      resolveAlert — marks an alert as resolved.----Understand How????????
      Sets is_resolved = 1 and records the resolution timestamp.
    */
    public void resolveAlert(int alertId) {
        final String sql = """
                UPDATE alerts
                SET is_resolved = 1, resolved_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, alertId);
            stmt.executeUpdate();
            System.out.println("[Alert] Resolved alert ID: " + alertId);

        } catch (SQLException e) {
            System.err.println("[Alert] Error resolving alert: " + e.getMessage());
        }
    }
}
