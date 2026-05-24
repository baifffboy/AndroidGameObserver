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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        recyclerView = findViewById(R.id.leaderboardRecyclerView);
        backButton = findViewById(R.id.backButton);

        adapter = new LeaderboardAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Показываем кэшированные данные
        if (!cachedLeaders.isEmpty()) {
            adapter.setLeaders(cachedLeaders);
        } else {
            Toast.makeText(this, "Нет данных. Запустите игру.", Toast.LENGTH_LONG).show();
        }

        backButton.setOnClickListener(v -> finish());
    }

    public static void updateCache(List<Player> leaders) {
        cachedLeaders = new ArrayList<>(leaders);
    }
}