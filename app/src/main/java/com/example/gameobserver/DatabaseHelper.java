package com.example.gameobserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public static List<Player> getLeaderboard(String serverIp, int serverPort) throws Exception {
        List<Player> leaders = new ArrayList<>();

        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_LEADERBOARD");

            String response;
            while ((response = in.readLine()) != null) {
                if (response.equals("END_LEADERBOARD")) break;

                if (response.startsWith("LEADER:")) {
                    String[] parts = response.split(":");
                    if (parts.length >= 3) {
                        String name = parts[1];
                        int wins = Integer.parseInt(parts[2]);
                        leaders.add(new Player(name, wins));
                    }
                }
            }
        }

        return leaders;
    }
}