package com.example.gameobserver;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NetworkThread.GameUpdateListener {

    private RecyclerView recyclerView;
    private PlayerAdapter adapter;
    private TextView statusText;
    private Button btnLeaderboard;
    private NetworkThread networkThread;
    private GameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();

        gameState = new GameState();
        startNetworkConnection();

        btnLeaderboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        statusText = findViewById(R.id.statusText);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
    }

    private void setupRecyclerView() {
        adapter = new PlayerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void startNetworkConnection() {
        networkThread = new NetworkThread(gameState, this);
        networkThread.start();
    }

    @Override
    public void onPlayersUpdated() {
        List<Player> playerList = new ArrayList<>(gameState.getPlayers().values());
        adapter.setPlayers(playerList);
        System.out.println("Обновлен список игроков, всего: " + playerList.size());
    }

    @Override
    public void onLeaderboardUpdated(List<Player> leaders) {
        System.out.println("Получено лидеров: " + leaders.size());
        LeaderboardActivity.updateCache(leaders);
    }

    @Override
    public void onConnectionError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            statusText.setText("Ошибка подключения");
            System.err.println("Ошибка подключения: " + error);
        });
    }

    @Override
    public void onGameStatusChanged(boolean active) {
        runOnUiThread(() -> {
            if (active) {
                statusText.setText("игра в процессе");
            } else {
                statusText.setText("игра не активна");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkThread != null) {
            networkThread.disconnect();
        }
    }
}