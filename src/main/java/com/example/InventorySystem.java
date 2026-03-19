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
}