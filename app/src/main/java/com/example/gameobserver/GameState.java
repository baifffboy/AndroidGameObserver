package com.example.gameobserver;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private boolean gameActive = false;
    private int roomId = 1;

    private int makeKey(int roomId, int playerId) {
        return roomId * 100 + playerId;
    }

    public void addOrUpdatePlayer(int roomId, int playerId, String name, int score, int shots) {
        int key = makeKey(roomId, playerId);
        Player player = players.get(key);
        if (player == null) {
            players.put(key, new Player(playerId, name, score, shots, roomId));
        } else {
            player.setScore(score);
            player.setShots(shots);
            if (name != null && !name.isEmpty()) {
                Player newPlayer = new Player(playerId, name, score, shots, roomId);
                players.put(key, newPlayer);
            }
        }
    }

    public ConcurrentHashMap<Integer, Player> getPlayers() {
        return players;
    }

    public void setGameActive(boolean active) {
        this.gameActive = active;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}