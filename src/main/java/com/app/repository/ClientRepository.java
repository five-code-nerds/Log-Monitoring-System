package com.app.repository;

import com.app.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {


    public void registerClient(String clientId, String hostname, String ipAddress) {
        final String sql = """
                INSERT INTO clients (client_id, hostname, ip_address, status)
                VALUES (?, ?, ?, 'ONLINE')
                ON DUPLICATE KEY UPDATE
                    status     = 'ONLINE',
                    ip_address = VALUES(ip_address),
                    last_seen  = CURRENT_TIMESTAMP
                """;


        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, clientId);
            stmt.setString(2, hostname);
            stmt.setString(3, ipAddress);
            stmt.executeUpdate();
            System.out.println("[ClientDAO] Registered client: " + clientId);

        } catch (SQLException e) {
            System.err.println("[Client] Error registering client: " + e.getMessage());
        }
    }

    public void setClientStatus(String clientId, String status) {
        final String sql = "UPDATE clients SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE client_id = ?";

        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement stmt = con.prepareStatement(sql);

            stmt.setString(1, status);
            stmt.setString(2, clientId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ClientDAO] Error updating status: " + e.getMessage());
        }
    }

    public List<String[]> getAllClients() {
        List<String[]> clients = new ArrayList<>();
        final String sql = "SELECT client_id, hostname, ip_address, status, last_seen FROM clients";

        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                        rs.getString("client_id"),
                        rs.getString("hostname"),
                        rs.getString("ip_address"),
                        rs.getString("status"),
                        rs.getString("last_seen")
                };
                clients.add(row);
            }

        } catch (SQLException e) {
            System.err.println("[Client] Error fetching clients: " + e.getMessage());
        }

        return clients;
    }
}