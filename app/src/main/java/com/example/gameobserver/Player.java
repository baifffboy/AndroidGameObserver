package com.example.gameobserver;

public class Player {
    private final int id;
    private final String name;
    private int score;
    private int shots;
    private int wins;
    private int roomId;

    public Player(int id, String name, int score, int shots, int roomId) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.shots = shots;
        this.wins = 0;
        this.roomId = roomId;
    }

    public Player(String name, int wins) {
        this.name = name;
        this.wins = wins;
        this.id = -1;
        this.score = 0;
        this.shots = 0;
        this.roomId = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public int getShots() { return shots; }
    public int getWins() { return wins; }
    public int getRoomId() { return roomId; }

    public void setScore(int score) { this.score = score; }
    public void setShots(int shots) { this.shots = shots; }
    public void setWins(int wins) { this.wins = wins; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getDisplayName() {
        return name + " (ком." + roomId + ")";
    }
}