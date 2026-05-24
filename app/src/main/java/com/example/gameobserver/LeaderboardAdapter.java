package com.example.gameobserver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<Player> leaders = new ArrayList<>();

    public void setLeaders(List<Player> leaders) {
        this.leaders = leaders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = leaders.get(position);
        holder.playerName.setText(player.getName());
        holder.playerWins.setText(String.valueOf(player.getWins()));
    }

    @Override
    public int getItemCount() {
        return leaders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView playerName, playerWins;

        ViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerName);
            playerWins = itemView.findViewById(R.id.playerWins);
        }
    }
}