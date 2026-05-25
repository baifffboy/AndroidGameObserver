package com.example.gameobserver;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkThread extends Thread {
    private static final String SERVER_IP = "192.168.1.168";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Handler uiHandler;
    private final GameState gameState;
    private volatile boolean running = true;
    private final String observerName;
    private Timer leaderboardTimer;

    public interface GameUpdateListener {
        void onPlayersUpdated();

        void onLeaderboardUpdated(List<Player> leaders);

        void onConnectionError(String error);

        void onGameStatusChanged(boolean active);
    }

    private final GameUpdateListener listener;

    public NetworkThread(GameState gameState, GameUpdateListener listener) {
        this.gameState = gameState;
        this.listener = listener;
        this.uiHandler = new Handler(Looper.getMainLooper());
        this.observerName = "AndroidObserver_" + System.currentTimeMillis();
    }

    public void startAutoLeaderboardUpdate() {
        if (leaderboardTimer != null) {
            leaderboardTimer.cancel();
        }
        leaderboardTimer = new Timer(true);
        leaderboardTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    requestLeaderboard();
                }
            }
        }, 1000, 1000);
    }

    public void stopAutoLeaderboardUpdate() {
        if (leaderboardTimer != null) {
            leaderboardTimer.cancel();
            leaderboardTimer = null;
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(observerName + ":0:1");
            String response = in.readLine();

            if (response != null && response.startsWith("OK:")) {
                String[] parts = response.split(":");
                int roomId = Integer.parseInt(parts[2]);
                gameState.setRoomId(roomId);

                uiHandler.post(() -> listener.onGameStatusChanged(true));

                String msg;
                while (running && (msg = in.readLine()) != null) {
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
            int playerId = Integer.parseInt(parts[1]);
            String name = parts[2];
            int roomId = Integer.parseInt(parts[3]);
            gameState.addOrUpdatePlayer(roomId, playerId, name, 0, 0);
            uiHandler.post(() -> listener.onPlayersUpdated());
        } else if (msg.startsWith("SCORE:")) {
            String[] parts = msg.split(":");
            int playerId = Integer.parseInt(parts[1]);
            int score = Integer.parseInt(parts[2]);
            int shots = Integer.parseInt(parts[3]);
            String name = parts[4];
            int roomId = Integer.parseInt(parts[5]);
            gameState.addOrUpdatePlayer(roomId, playerId, name, score, shots);
            uiHandler.post(() -> listener.onPlayersUpdated());
        } else if (msg.startsWith("WINNER:")) {
            gameState.setGameActive(false);
            uiHandler.post(() -> {
                listener.onGameStatusChanged(false);
                listener.onPlayersUpdated();
            });
        } else if (msg.equals("START")) {
            gameState.setGameActive(true);
            uiHandler.post(() -> listener.onGameStatusChanged(true));
        } else if (msg.startsWith("STOP:")) {
            gameState.setGameActive(false);
            uiHandler.post(() -> {
                listener.onGameStatusChanged(false);
                listener.onPlayersUpdated();
            });
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
        return DirectDatabaseHelper.getLeaderboardDirect();
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