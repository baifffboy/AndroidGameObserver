package com.example.gameobserver;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private boolean gameActive = false;
    private int roomId = 1;

    public void addOrUpdatePlayer(int id, String name, int score, int shots) {
        Player player = players.get(id);
        if (player == null) {
            players.put(id, new Player(id, name, score, shots));
        } else {
            player.setScore(score);
            player.setShots(shots);
            if (name != null && !name.isEmpty()) {
                Player newPlayer = new Player(id, name, score, shots);
                players.put(id, newPlayer);
            }
        }
    }

    public void removePlayer(int id) {
        players.remove(id);
    }

    public ConcurrentHashMap<Integer, Player> getPlayers() {
        return players;
    }

    public void setGameActive(boolean active) {
        this.gameActive = active;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public void clear() {
        players.clear();
        gameActive = false;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomId() {
        return roomId;
    }
}