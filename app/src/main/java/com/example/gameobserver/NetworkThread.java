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
    private static final String SERVER_IP = "192.168.1.168";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Handler uiHandler;
    private final GameState gameState;
    private volatile boolean running = true;

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
    }

    @Override
    public void run() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Регистрируемся как наблюдатель
            out.println("Observer_" + System.currentTimeMillis() + ":0:1");

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

        } else if (msg.startsWith("SCORE:")) {
            String[] parts = msg.split(":");
            int id = Integer.parseInt(parts[1]);
            int score = Integer.parseInt(parts[2]);
            int shots = Integer.parseInt(parts[3]);
            String name = parts.length > 4 ? parts[4] : "";

            gameState.addOrUpdatePlayer(id, name, score, shots);
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
            }
        }).start();
    }

    private List<Player> getLeaderboardFromServer() throws Exception {
        List<Player> leaders = new ArrayList<>();

        try (Socket s = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter pout = new PrintWriter(s.getOutputStream(), true);
             BufferedReader bin = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            pout.println("GET_LEADERBOARD");

            String response;
            while ((response = bin.readLine()) != null) {
                if (response.equals("END_LEADERBOARD")) break;
                if (response.startsWith("LEADER:")) {
                    String[] parts = response.split(":");
                    if (parts.length >= 3) {
                        leaders.add(new Player(parts[1], Integer.parseInt(parts[2])));
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