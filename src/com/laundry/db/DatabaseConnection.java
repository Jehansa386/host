package com.laundry.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // MySQL connection properties (default credentials for XAMPP)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/laundry_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load MySQL JDBC Driver: " + e.getMessage());
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                System.out.println("Initializing MySQL database inside XAMPP...");
                Statement stmt = conn.createStatement();

                // Create Customer Table
                stmt.execute("CREATE TABLE IF NOT EXISTS Customer (" +
                        "customer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "phone VARCHAR(100) NOT NULL UNIQUE, " +
                        "address VARCHAR(255), " +
                        "password VARCHAR(255) NOT NULL DEFAULT 'customer123'" +
                        ");");

                // Create Service Table
                stmt.execute("CREATE TABLE IF NOT EXISTS Service (" +
                        "service_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "service_name VARCHAR(100) NOT NULL UNIQUE, " +
                        "price DOUBLE NOT NULL" +
                        ");");

                // Create Laundry_Order Table
                stmt.execute("CREATE TABLE IF NOT EXISTS Laundry_Order (" +
                        "order_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "customer_id INT, " +
                        "order_date VARCHAR(100) NOT NULL, " +
                        "total DOUBLE NOT NULL, " +
                        "status VARCHAR(50) NOT NULL, " +
                        "FOREIGN KEY(customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE" +
                        ");");

                // Create Order_Item Table
                stmt.execute("CREATE TABLE IF NOT EXISTS Order_Item (" +
                        "item_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "order_id INT, " +
                        "service_id INT, " +
                        "quantity INT NOT NULL, " +
                        "subtotal DOUBLE NOT NULL, " +
                        "FOREIGN KEY(order_id) REFERENCES Laundry_Order(order_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(service_id) REFERENCES Service(service_id) ON DELETE SET NULL" +
                        ");");

                // Create Payment Table
                stmt.execute("CREATE TABLE IF NOT EXISTS Payment (" +
                        "payment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "order_id INT, " +
                        "amount DOUBLE NOT NULL, " +
                        "payment_date VARCHAR(100) NOT NULL, " +
                        "payment_method VARCHAR(50) NOT NULL, " +
                        "FOREIGN KEY(order_id) REFERENCES Laundry_Order(order_id) ON DELETE CASCADE" +
                        ");");

                // Create User Table
                stmt.execute("CREATE TABLE IF NOT EXISTS User (" +
                        "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(100) NOT NULL UNIQUE, " +
                        "password VARCHAR(100) NOT NULL" +
                        ");");

                // Seed Default Services
                seedServices(conn);

                // Seed Default Admin User
                seedAdminUser(conn);

                System.out.println("MySQL database initialization and seeds completed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("MySQL database initialization failed: " + e.getMessage());
        }
    }

    private static void seedServices(Connection conn) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM Service";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding default services...");
                String insertSQL = "INSERT INTO Service (service_name, price) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, "Wash");
                    pstmt.setDouble(2, 2.50);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Dry Clean");
                    pstmt.setDouble(2, 8.00);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Iron");
                    pstmt.setDouble(2, 1.50);
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Wash & Iron");
                    pstmt.setDouble(2, 3.50);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    private static void seedAdminUser(Connection conn) throws SQLException {
        String countQuery = "SELECT COUNT(*) FROM User WHERE username = 'admin'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding default admin user...");
                String insertSQL = "INSERT INTO User (username, password) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123");
                    pstmt.executeUpdate();
                }
            }
        }
    }
}
