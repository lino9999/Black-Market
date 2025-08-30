package com.Lino.blackMarket.managers;

import com.Lino.blackMarket.BlackMarket;

import java.sql.*;

public class DatabaseManager {

    private final BlackMarket plugin;
    private Connection connection;

    public DatabaseManager(BlackMarket plugin) {
        this.plugin = plugin;
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/blackmarket.db");

            createTables();
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("SQLite JDBC driver not found! Make sure it's included in the JAR.");
            e.printStackTrace();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database!");
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS purchases (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "item_id TEXT NOT NULL," +
                            "amount INTEGER NOT NULL," +
                            "day_number BIGINT NOT NULL," +
                            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePurchase(String itemId, int amount, long dayNumber) {
        String sql = "INSERT INTO purchases (item_id, amount, day_number) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setInt(2, amount);
            pstmt.setLong(3, dayNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTodayPurchases(String itemId, long dayNumber) {
        String sql = "SELECT SUM(amount) FROM purchases WHERE item_id = ? AND day_number = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setLong(2, dayNumber);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void cleanOldPurchases(long daysToKeep) {
        String sql = "DELETE FROM purchases WHERE day_number < ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, daysToKeep);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}