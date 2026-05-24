package com.example.gameobserver;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private Button backButton;
    private static List<Player> cachedLeaders = new ArrayList<>();
    private static LeaderboardActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        instance = this;

        recyclerView = findViewById(R.id.leaderboardRecyclerView);
        backButton = findViewById(R.id.backButton);

        adapter = new LeaderboardAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Показываем кэшированные данные
        if (!cachedLeaders.isEmpty()) {
            adapter.setLeaders(cachedLeaders);
        } else {
            // Показываем сообщение, что данных нет
            Toast.makeText(this, "Нет данных. Сыграйте хотя бы одну игру.", Toast.LENGTH_LONG).show();
        }

        backButton.setOnClickListener(v -> finish());
    }

    // Статический метод для обновления из MainActivity
    public static void updateDisplay(List<Player> leaders) {
        if (leaders != null) {
            cachedLeaders = new ArrayList<>(leaders);
        }
        if (instance != null && instance.adapter != null) {
            instance.runOnUiThread(() -> {
                instance.adapter.setLeaders(cachedLeaders);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}