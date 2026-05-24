package com.example.gameobserver;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkThread extends Thread {
    private static final String SERVER_IP = "192.168.1.168";  // ВАШ IP
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Handler uiHandler;
    private final GameState gameState;
    private volatile boolean running = true;
    private String observerName;

    public interface GameUpdateListener {
        void onPlayersUpdated();
        void onLeaderboardUpdated(List<Player> leaders);
        void onConnectionError(String error);
        void onGameStatusChanged(boolean active);
    }

    private GameUpdateListener listener;

    public NetworkThread(GameState gameState, GameUpdateListener listener) {
        this.gameState = gameState;
        this.listener = listener;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.observerName = "AndroidObserver_" + System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Регистрируемся как наблюдатель (ID 0)
            out.println(observerName + ":0:1");
            System.out.println("Отправлено регистрация: " + observerName + ":0:1");

            String response = in.readLine();
            System.out.println("Ответ сервера: " + response);

            if (response != null && response.startsWith("OK:")) {
                String[] parts = response.split(":");
                int roomId = Integer.parseInt(parts[2]);
                gameState.setRoomId(roomId);

                uiHandler.post(() -> listener.onGameStatusChanged(true));

                String msg;
                while (running && (msg = in.readLine()) != null) {
                    System.out.println("Получено от сервера: " + msg);
                    handleServerMessage(msg);
                }
            } else {
                uiHandler.post(() -> listener.onConnectionError("Ошибка: " + response));
            }

        } catch (Exception e) {
            e.printStackTrace();
            uiHandler.post(() -> listener.onConnectionError("Ошибка: " + e.getMessage()));
        } finally {
            disconnect();
        }
    }

    private void handleServerMessage(String msg) {
        if (msg == null) return;

        if (msg.startsWith("NEW_PLAYER:")) {
            String[] parts = msg.split(":");
            int id = Integer.parseInt(parts[1]);
            String name = parts[2];
            gameState.addOrUpdatePlayer(id, name, 0, 0);
            uiHandler.post(() -> listener.onPlayersUpdated());
            System.out.println("Добавлен игрок: " + name + " (id=" + id + ")");

        } else if (msg.startsWith("SCORE:")) {
            String[] parts = msg.split(":");
            int id = Integer.parseInt(parts[1]);
            int score = Integer.parseInt(parts[2]);
            int shots = Integer.parseInt(parts[3]);
            String name = parts.length > 4 ? parts[4] : "";

            gameState.addOrUpdatePlayer(id, name, score, shots);
            uiHandler.post(() -> listener.onPlayersUpdated());
            System.out.println("Обновлен счет игрока " + id + ": score=" + score + ", shots=" + shots);

        } else if (msg.startsWith("WINNER:")) {
            gameState.setGameActive(false);
            uiHandler.post(() -> {
                listener.onGameStatusChanged(false);
                listener.onPlayersUpdated();
            });
            System.out.println("Игра закончена, победитель: " + msg);

        } else if (msg.equals("START")) {
            gameState.setGameActive(true);
            uiHandler.post(() -> listener.onGameStatusChanged(true));
            System.out.println("Игра началась!");

        } else if (msg.startsWith("STOP:")) {
            gameState.setGameActive(false);
            uiHandler.post(() -> {
                listener.onGameStatusChanged(false);
                listener.onPlayersUpdated();
            });
            System.out.println("Игра остановлена");
        }
    }

    public void requestLeaderboard() {
        new Thread(() -> {
            try {
                List<Player> leaders = getLeaderboardFromServer();
                uiHandler.post(() -> listener.onLeaderboardUpdated(leaders));
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> listener.onLeaderboardUpdated(new ArrayList<>()));
            }
        }).start();
    }

    private List<Player> getLeaderboardFromServer() throws Exception {
        List<Player> leaders = new ArrayList<>();

        try (Socket s = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter pout = new PrintWriter(s.getOutputStream(), true);
             BufferedReader bin = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            pout.println("GET_LEADERBOARD");
            System.out.println("Запрос таблицы лидеров отправлен");

            String response;
            while ((response = bin.readLine()) != null) {
                System.out.println("Ответ таблицы: " + response);
                if (response.equals("END_LEADERBOARD")) break;
                if (response.startsWith("LEADER:")) {
                    String[] parts = response.split(":");
                    if (parts.length >= 3) {
                        String name = parts[1];
                        int wins = Integer.parseInt(parts[2]);
                        leaders.add(new Player(name, wins));
                        System.out.println("Лидер: " + name + " - " + wins + " побед");
                    }
                }
            }
        }

        return leaders;
    }

    public void disconnect() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}