package com.app.database;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/log_monitor";
    private static String USER = "root";
    private static String PASSWORD;
    private static Dotenv dotenv;

    // The single shared connection object

    private static Connection connection = null;

    static  {
        dotenv = Dotenv.load();
        DatabaseConnection.PASSWORD = dotenv.get("DB_PASSWORD");
    }
    public DatabaseConnection() {}


    public static Connection getConnection() throws SQLException {

        if (connection == null|| connection.isClosed()) {

            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Connected to MySQL database successfully.");

        }

        return connection;

    }


    public static void closeConnection(){
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }


}

