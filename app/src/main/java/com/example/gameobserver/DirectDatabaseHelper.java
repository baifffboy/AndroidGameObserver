package com.example.gameobserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DirectDatabaseHelper {
    private static final String DB_URL = "jdbc:h2:tcp://10.0.2.2:9092/~/shared_game_db";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public static List<Player> getLeaderboardDirect() {
        List<Player> leaders = new ArrayList<>();

        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();

            String query = "SELECT playerName, wins FROM players ORDER BY wins DESC";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("playerName");
                int wins = rs.getInt("wins");
                leaders.add(new Player(name, wins));
                System.out.println("Загружен игрок: " + name + " - " + wins + " побед");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            e.printStackTrace();
        }

        return leaders;
    }
}