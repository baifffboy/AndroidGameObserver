package com.example.gameobserver;

import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    private boolean gameActive = false;
    private int roomId = 1;

    // Уникальный ключ = комната * 100 + id игрока
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

    public void removePlayer(int roomId, int playerId) {
        int key = makeKey(roomId, playerId);
        players.remove(key);
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