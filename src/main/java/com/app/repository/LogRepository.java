package com.app.repository;

import com.app.database.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedWriter;


public class LogRepository {

    /*
     insertLog — saves a single log entry to the database.
     Returns the auto-generated ID of the inserted row.
     We need this ID so the AlertDAO can link an alert
     back to the specific log that triggered it.
     */
    public int insertLog(String clientId, String logLevel,
                         String message, String sourceFile) {

        final String sql = "INSERT INTO logs (client_id, log_level, message, source_file) VALUES (?, ?, ?, ?)";
        int generatedId = -1; // -1 means insert failed

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, clientId);
            stmt.setString(2, logLevel);
            stmt.setString(3, message);
            stmt.setString(4, sourceFile);
            stmt.executeUpdate();

            // Retrieve the auto-generated primary key
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("[Log] Error inserting log: " + e.getMessage());
        }

        return generatedId;
    }

    public List<String[]> getRecentLogs(int limit) {
        List<String[]> logs = new ArrayList<>();
        final String sql = """
                SELECT id, client_id, log_level, message, source_file, logged_at
                FROM logs
                ORDER BY logged_at DESC
                LIMIT ?
                """;

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String[] row = {
                            rs.getString("id"),
                            rs.getString("client_id"),
                            rs.getString("log_level"),
                            rs.getString("message"),
                            rs.getString("source_file"),
                            rs.getString("logged_at")
                    };
                    logs.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[Log] Error fetching logs: " + e.getMessage());
        }

        return logs;
    }

    /*
     searchLogs — filter logs by client, level, and/or keyword.
     Dynamic SQL with LIKE '%keyword%' searches for keyword anywhere in the message.
     The % is a wildcard meaning "any characters before/after".
     Passing null for a filter means "don't filter by this field".
     */
    public List<String[]> searchLogs(String clientId, String logLevel, String keyword) {
        List<String[]> logs = new ArrayList<>();

        // Build query dynamically based on which filters are provided
        StringBuilder sql = new StringBuilder(
                "SELECT id, client_id, log_level, message, logged_at FROM logs WHERE 1=1"
        );


        if (clientId != null && !clientId.isEmpty())
            sql.append(" AND client_id = ?");
        if (logLevel != null && !logLevel.isEmpty())
            sql.append(" AND log_level = ?");
        if (keyword != null && !keyword.isEmpty())
            sql.append(" AND message LIKE ?");

        sql.append(" ORDER BY logged_at DESC LIMIT 200");

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql.toString());

            // Set parameters in the same order we appended them
            int idx = 1;
            if (clientId != null && !clientId.isEmpty())
                stmt.setString(idx++, clientId);
            if (logLevel != null && !logLevel.isEmpty())
                stmt.setString(idx++, logLevel);
            if (keyword != null && !keyword.isEmpty())
                stmt.setString(idx++, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String[] row = {
                            rs.getString("id"),
                            rs.getString("client_id"),
                            rs.getString("log_level"),
                            rs.getString("message"),
                            rs.getString("logged_at")
                    };
                    logs.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("[Log] Error searching logs: " + e.getMessage());
        }

        return logs;
    }

    /*
      exportLogsToCSV — writes filtered logs to a CSV file.
    */

    public void exportLogsToCSV(String filePath) {
        List<String[]> logs = getRecentLogs(1000);

        try {
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);

            // Write CSV header
            bw.write("ID, ClientID, Level, Message, SourceFile, LoggedAt");
            bw.newLine();

            for (String[] row : logs) {
                // Wrap message in quotes in case it contains commas
                bw.write(String.join(",",
                        row[0], row[1], row[2],
                        "\"" + row[3] + "\"",
                        row[4], row[5]
                ));
                bw.newLine();
            }

            System.out.println("[Log] Exported logs to: " + filePath);

        } catch (IOException e) {
            System.err.println("[Log] Export error: " + e.getMessage());
        }
    }
}
