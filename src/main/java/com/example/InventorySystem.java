package com.example;

import java.sql.*;
import java.util.List;

class InventorySystem {
    private Connection conn;

    // error handling if conn is closed
    public void close() throws SQLException {
        conn.close();
    }
    // open connection to database using constructor
    public InventorySystem() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/login?useSSL=false&allowPublicKeyRetrieval=true",
                "root",
                "abc"
        );
    }

    // constructor for unit testing with mock objects
    public InventorySystem(Connection conn) {
        this.conn = conn;
    }

    // method to add item to database
    public void addItem(int id, String name, double price, int quantity, String origin) {

        logToDB("Added item: " + name);

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "INSERT INTO Inventory (idinventory, name, price, quantity, origin) Values (?, ?, ?, ?, ?)"
            );
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setDouble(3, price);
            preparedStatement.setInt(4, quantity);
            preparedStatement.setString(5, origin);

            int rowsAffected = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // method to remove an item from database
    public void removeItem(String name) {

        logToDB("Removed item: " + name);

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "DELETE FROM inventory WHERE name = ?"
            );
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update price of Product in database
    public void updatePrice(String name, double newPrice) {
        try {
            double oldPrice = 0.0;

            PreparedStatement selectStatement = conn.prepareStatement(
                    "SELECT price FROM inventory WHERE name = ?"
            );
            selectStatement.setString(1, name);

            ResultSet rs = selectStatement.executeQuery();
            if (rs.next()){
                oldPrice = rs.getDouble("price");
            }

            PreparedStatement preparedStatement = conn.prepareStatement(
                    "UPDATE inventory SET price = ? WHERE name = ?"
            );
            logToDB("Updated price for: " + name + " from " + oldPrice + " to " + newPrice);
            preparedStatement.setDouble(1, newPrice);
            preparedStatement.setString(2, name);
            preparedStatement.executeUpdate();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the quantity of product in database
    public void updateQuantity(String name, int newQuantity) {

        try {
            int oldQuantity = 0;

            PreparedStatement selectStatement = conn.prepareStatement(
                    "SELECT quantity FROM inventory WHERE name = ?"
            );
            selectStatement.setString(1, name);

            ResultSet rs = selectStatement.executeQuery();
            if (rs.next()){
                oldQuantity = rs.getInt("quantity");
            }

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE inventory SET quantity = ? WHERE name = ?"
            );
            ps.setInt(1, newQuantity);
            ps.setString(2, name);
            ps.executeUpdate();

            logToDB("Updated quantity for: " + name + " from " + oldQuantity + " to " + newQuantity);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // load all inventory into data list of products
    public void loadInventory(List<Product> Data) {
        Data.clear();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM inventory");
            while (rs.next()) {
                Data.add(new Product(rs.getInt("idinventory"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("origin")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // If login match return role
    public String login(String username, String password) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users");
            while (rs.next()) {
                if (rs.getString("username").equals(username)){
                    if (rs.getString("password").equals(password)){
                        return rs.getString("role");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Insert log into database
    private void logToDB(String entries) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO log (entries) VALUES (?)"
            );
            ps.setString(1, entries);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve log and add it to list of logs 
    public void loadLog(List<Log> logs) {
        logs.clear();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT idlog, entries, timestamp FROM log ORDER BY idlog DESC"
            );
            while (rs.next()) {
                logs.add(new Log(rs.getInt("idlog"), rs.getString("entries"), rs.getString("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isTableEmpty(String tableName) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName + " LIMIT 1");

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // Create all necessary tables
    public void initializeDatabases(){
        try {
            Statement createstatement = conn.createStatement();

            // Create inventory table
            createstatement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS inventory (
                    idinventory INT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    price DOUBLE NOT NULL,
                    quantity INT NOT NULL,
                    origin VARCHAR(255) NOT NULL
                )                
            """);

            // Create log table
            createstatement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS log (
                    idlog INT NOT NULL AUTO_INCREMENT,
                    entries VARCHAR(255) NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (idlog)
                )                
            """);

            // Create users table
            createstatement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(255) NOT NULL
                )                
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add user to database
    public void addUser(String username, String password, String role) {

        logToDB("Login created for " + username + " (" + role + ")");

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) Values (?, ?, ?)"
            );
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);

            int rowsAffected = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove user from database
    public void removeUser(String username) {

        logToDB("Removed user " + username);

        try {
            PreparedStatement preparedStatement = conn.prepareStatement(
                    "DELETE FROM users WHERE username = ?"
            );
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get role of username
    public String getRole(String username) {
        String role = null;
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT role FROM users WHERE username = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }
}