package com.example.madproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private TextView messageTextView;
    private FavoritesAdapter adapter;
    private final List<Pokemon> favoriteList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecycler);
        messageTextView = findViewById(R.id.messageTextView);

        favoritesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new FavoritesAdapter(this, favoriteList, new FavoritesAdapter.OnItemClickListener() {
            public void onItemClick(Pokemon pokemon) {
                // Optional: Navigate to detail screen
            }
            public void onRemoveClick(Pokemon pokemon) {
                new androidx.appcompat.app.AlertDialog.Builder(FavoritesActivity.this)
                        .setTitle("Remove Favorite")
                        .setMessage("Are you sure you want to remove " + capitalize(pokemon.getName()) + " from your favorites?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
                            prefs.edit().remove(pokemon.getName()).apply();
                            favoriteList.remove(pokemon);
                            adapter.notifyDataSetChanged();
                            checkIfEmpty();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        favoritesRecyclerView.setAdapter(adapter);
        loadFavorites();

        BottomNavigationView navView = findViewById(R.id.bottomNavigation);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.typesnav) {
                startActivity(new Intent(this, PokemonTypeLibraryActivity.class));
                return true;
            } else if (id == R.id.homefavnav) {
                return true;
            } else if (id == R.id.homenav) {
                startActivity(new Intent(this, Pokedex.class));
                return true;
            } else if (id == R.id.logoutnav) {
                getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit().clear().apply();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadFavorites() {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        Map<String, ?> allFavorites = prefs.getAll();

        favoriteList.clear();

        for (Map.Entry<String, ?> entry : allFavorites.entrySet()) {
            if ((Boolean) entry.getValue()) {
                fetchPokemonDetails(entry.getKey());
            }
        }

        checkIfEmpty();
    }

    private void fetchPokemonDetails(String name) {
        String url = "https://pokeapi.co/api/v2/pokemon/" + name.toLowerCase();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(FavoritesActivity.this, "Failed to load " + name, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                try {
                    JSONObject json = new JSONObject(response.body().string());

                    int id = json.getInt("id");
                    String imageUrl = json.getJSONObject("sprites").getJSONObject("other")
                            .getJSONObject("official-artwork").getString("front_default");

                    JSONArray typesArray = json.getJSONArray("types");
                    List<String> typeList = new ArrayList<>();
                    for (int i = 0; i < typesArray.length(); i++) {
                        String type = typesArray.getJSONObject(i).getJSONObject("type").getString("name");
                        typeList.add(type);
                    }

                    Pokemon pokemon = new Pokemon(name, id, imageUrl);
                    pokemon.setType(typeList);

                    runOnUiThread(() -> {
                        favoriteList.add(pokemon);
                        adapter.notifyDataSetChanged();
                        checkIfEmpty();
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkIfEmpty() {
        runOnUiThread(() -> {
            if (favoriteList.isEmpty()) {
                messageTextView.setText("No favorites added yet.");
                messageTextView.setVisibility(View.VISIBLE);
            } else {
                messageTextView.setVisibility(View.GONE);
            }
        });
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
