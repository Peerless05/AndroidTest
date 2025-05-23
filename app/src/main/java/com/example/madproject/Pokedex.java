package com.example.madproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Pokedex extends AppCompatActivity {

    private EditText input;
    private Button search, clear;
    private ImageButton unfav;
    private TextView idText, nameText, type1Text, type2Text;
    private TextView hpText, attackText, defenseText, specialAttackText, specialDefenseText, speedText;
    private ImageView imageView;
    private ProgressBar hpBar, attackBar, defenseBar, specialAttackBar, specialDefenseBar, speedBar;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        input = findViewById(R.id.enter);
        search = findViewById(R.id.search);
        clear = findViewById(R.id.clear);
        unfav = findViewById(R.id.unfav);
        idText = findViewById(R.id.id);
        nameText = findViewById(R.id.name);
        type1Text = findViewById(R.id.type1);
        type2Text = findViewById(R.id.type2);
        hpText = findViewById(R.id.hp_label);
        attackText = findViewById(R.id.attack_label);
        defenseText = findViewById(R.id.defense_label);
        specialAttackText = findViewById(R.id.special_label);
        specialDefenseText = findViewById(R.id.special_defense_label);
        speedText = findViewById(R.id.speed_label);
        imageView = findViewById(R.id.image);
        hpBar = findViewById(R.id.hp_bar);
        attackBar = findViewById(R.id.attack_bar);
        defenseBar = findViewById(R.id.defense_bar);
        specialAttackBar = findViewById(R.id.special_bar);
        specialDefenseBar = findViewById(R.id.special_defense_bar);
        speedBar = findViewById(R.id.speed_bar);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.fav);
        badgeDrawable.setNumber(4);   //Change me depende sa dami ng pokemon na meron si User if kaya
        badgeDrawable.setVisible(true);

        BottomNavigationView navView = findViewById(R.id.bottomNavigation); // your view ID
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.typesnav) {
               // Toast.makeText(this, "Types", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Pokedex.this, PokemonTypeLibraryActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.homefavnav) {
                // Navigate to Favorites Activity
                //Toast.makeText(this, "Favorites", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.homenav) {
                //Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, Pokedex.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (id == R.id.clearnav) {
                resetUI();
                //Toast.makeText(Pokedex.this, "Cleared", Toast.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.logoutnav) {
                getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply();
               // Toast.makeText(Pokedex.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Pokedex.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });


        search.setOnClickListener(v -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                resetProgressBars();
                fetchPokemonData(query);
            } else {
                Toast.makeText(this, "Enter Pokémon name or ID", Toast.LENGTH_SHORT).show();
            }
        });
        unfav.setOnClickListener(v -> {
            String name = nameText.getText().toString().replace("NAME 👆: ", "");
            if (!name.equals("Name")) {
                saveToFavorites(name);
                Toast.makeText(Pokedex.this, name + " Added to Favorites!", Toast.LENGTH_SHORT).show();
            }
        });

        clear.setOnClickListener(v -> resetUI());
    }

    private void resetUI() {
        input.setText("");
        resetProgressBars();
        idText.setText("ID# 000");
        nameText.setText("Name");
        nameText.setTextColor(Color.GRAY);
        type1Text.setText("Type");
        type2Text.setText("Type");
        type1Text.setVisibility(View.GONE);
        type2Text.setVisibility(View.GONE);
        imageView.setImageResource(R.drawable.pokeballgif); // Ensure you have this drawable
    }

    private void resetProgressBars() {
        hpBar.setProgress(0);
        attackBar.setProgress(0);
        defenseBar.setProgress(0);
        specialAttackBar.setProgress(0);
        specialDefenseBar.setProgress(0);
        speedBar.setProgress(0);

        hpText.setText("HP 💚: 0");
        attackText.setText("Attack ⚔: 0");
        defenseText.setText("Defense 🛡: 0");
        specialAttackText.setText("Special Attack 🔥: 0");
        specialDefenseText.setText("Special Defense 🔲: 0");
        speedText.setText("Speed 💨: 0");
    }

    private void saveToFavorites(String pokemonName) {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(pokemonName.toLowerCase(), true); // You can also store JSON if more data is needed
        editor.apply();
    }

    private void showLoading() {
        progressDialog = new ProgressDialog(Pokedex.this);
        progressDialog.setMessage("Fetching Pokémon data...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void fetchPokemonData(String query) {
        showLoading();

        new Thread(() -> {
            try {
                String url = "https://pokeapi.co/api/v2/pokemon/" + query.toLowerCase();
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JSONObject response = new JSONObject(stringBuilder.toString());

                runOnUiThread(() -> {
                    updateUI(response);
                    hideLoading();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(Pokedex.this, "Failed to fetch data. Please check the Pokémon name or ID.", Toast.LENGTH_SHORT).show();
                    hideLoading();
                });
            }
        }).start();
    }

    private void updateUI(JSONObject pokemon) {
        try {
            int id = pokemon.getInt("id");
            String name = pokemon.getString("name");
            JSONArray types = pokemon.getJSONArray("types");

            type1Text.setVisibility(View.GONE);
            type2Text.setVisibility(View.GONE);

            if (types.length() > 0) {
                JSONObject type1 = types.getJSONObject(0).getJSONObject("type");
                String rawType1 = type1.getString("name");
                type1Text.setText(capitalize(rawType1));
                setRoundedCorners(type1Text);
                type1Text.setVisibility(View.VISIBLE);
            }

            if (types.length() > 1) {
                JSONObject type2 = types.getJSONObject(1).getJSONObject("type");
                String rawType2 = type2.getString("name");
                type2Text.setText(capitalize(rawType2));
                setRoundedCorners(type2Text);
                type2Text.setVisibility(View.VISIBLE);
            }

            idText.setText("ID# " + id);
            nameText.setText("NAME 👆: " + capitalize(name));

            JSONArray stats = pokemon.getJSONArray("stats");
            for (int i = 0; i < stats.length(); i++) {
                JSONObject stat = stats.getJSONObject(i);
                String statName = stat.getJSONObject("stat").getString("name");
                int baseStat = stat.getInt("base_stat");

                switch (statName) {
                    case "hp":
                        hpBar.setProgress(baseStat);
                        hpText.setText("HP 💚: " + baseStat);
                        break;
                    case "attack":
                        attackBar.setProgress(baseStat);
                        attackText.setText("Attack ⚔: " + baseStat);
                        break;
                    case "defense":
                        defenseBar.setProgress(baseStat);
                        defenseText.setText("Defense 🛡: " + baseStat);
                        break;
                    case "special-attack":
                        specialAttackBar.setProgress(baseStat);
                        specialAttackText.setText("Special Attack 🔥: " + baseStat);
                        break;
                    case "special-defense":
                        specialDefenseBar.setProgress(baseStat);
                        specialDefenseText.setText("Special Defense 🔲: " + baseStat);
                        break;
                    case "speed":
                        speedBar.setProgress(baseStat);
                        speedText.setText("Speed 💨: " + baseStat);
                        break;
                }
            }

            JSONObject sprites = pokemon.getJSONObject("sprites");
            JSONObject other = sprites.getJSONObject("other");
            JSONObject officialArtwork = other.getJSONObject("official-artwork");
            String spriteUrl = officialArtwork.getString("front_default");

            Glide.with(this)
                    .load(spriteUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(imageView);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Pokedex.this, "Error parsing data. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private void setRoundedCorners(TextView textView) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.LTGRAY);
        drawable.setCornerRadius(16f);
        textView.setBackground(drawable);
    }
}