// LeaderboardActivity.java
package com.example.gamebyna;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    private koneksi db;
    private RecyclerView recyclerLeaderboard;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_CURRENT_USER = "current_user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        db = new koneksi(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        recyclerLeaderboard = findViewById(R.id.recyclerLeaderboard);
        recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(this));

        loadData();

        // Tombol back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            String user = prefs.getString(KEY_CURRENT_USER, "");
            if (user.equalsIgnoreCase("admin")) {
                startActivity(new Intent(LeaderboardActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(LeaderboardActivity.this, MainActivity.class));
            }
            finish();
        });
    }

    private void loadData() {
        Cursor c = db.getLeaderboard();
        ArrayList<UserScore> list = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String user = c.getString(0);
                int pts     = c.getInt(1);
                list.add(new UserScore(user, pts));
            } while (c.moveToNext());
        }
        c.close();
        recyclerLeaderboard.setAdapter(new LeaderboardAdapter(list));
    }

    private static class UserScore {
        final String username;
        final int points;
        UserScore(String u, int p) {
            this.username = u;
            this.points   = p;
        }
    }

    private static class LeaderboardAdapter
            extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

        private final ArrayList<UserScore> data;
        LeaderboardAdapter(ArrayList<UserScore> list) {
            data = list;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            UserScore us = data.get(position);
            holder.pos.setText(String.valueOf(position + 1));
            holder.user.setText(us.username);
            holder.points.setText(String.valueOf(us.points));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView pos, user, points;
            VH(View itemView) {
                super(itemView);
                pos    = itemView.findViewById(R.id.textPosition);
                user   = itemView.findViewById(R.id.textUsername);
                points = itemView.findViewById(R.id.textPoints);
            }
        }
    }
}
