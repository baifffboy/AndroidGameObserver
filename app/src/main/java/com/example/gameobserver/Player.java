package com.example.gameobserver;

public class Player {
    private int id;
    private String name;
    private int score;      // попадания
    private int shots;      // выстрелы
    private int wins;       // победы

    public Player(int id, String name, int score, int shots) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.shots = shots;
        this.wins = 0;
    }

    public Player(String name, int wins) {
        this.name = name;
        this.wins = wins;
        this.id = -1;
        this.score = 0;
        this.shots = 0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public int getShots() { return shots; }
    public int getWins() { return wins; }

    public void setScore(int score) { this.score = score; }
    public void setShots(int shots) { this.shots = shots; }
    public void setWins(int wins) { this.wins = wins; }
}