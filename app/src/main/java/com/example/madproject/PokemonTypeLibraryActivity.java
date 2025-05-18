package com.example.madproject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PokemonTypeLibraryActivity extends AppCompatActivity {

    RecyclerView typeRecyclerView, pokemonRecyclerView;
    TextView messageTextView;

    PokemonTypeAdapter typeAdapter;
    PokemonAdapter pokemonAdapter;

    List<String> typeList = new ArrayList<>();
    List<Pokemon> pokemonList = new ArrayList<>();

    OkHttpClient client = new OkHttpClient();

    boolean typeClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_type_library);

        // Views
        typeRecyclerView = findViewById(R.id.typeRecyclerView);
        pokemonRecyclerView = findViewById(R.id.pokemonRecyclerView);
        messageTextView = findViewById(R.id.messageTextView);

        // Setup RecyclerViews
        typeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        typeAdapter = new PokemonTypeAdapter(typeList, this::loadPokemonByType);
        typeRecyclerView.setAdapter(typeAdapter);

        pokemonRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        pokemonAdapter = new PokemonAdapter(pokemonList);
        pokemonRecyclerView.setAdapter(pokemonAdapter);

        // Show default instruction
        showMessage("Please select a Pokémon type.");

        // Load types
        loadPokemonTypes();
    }

    private void loadPokemonTypes() {
        Request request = new Request.Builder()
                .url("https://pokeapi.co/api/v2/type")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PokemonTypeLibraryActivity.this, "Failed to load types", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray results = json.getJSONArray("results");

                    typeList.clear();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject type = results.getJSONObject(i);
                        String name = type.getString("name");
                        typeList.add(name);
                    }

                    runOnUiThread(() -> typeAdapter.notifyDataSetChanged());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadPokemonByType(String typeName) {
        typeClicked = true;
        showMessage("Loading Pokémon...");

        pokemonList.clear();
        runOnUiThread(() -> pokemonAdapter.notifyDataSetChanged());

        Request request = new Request.Builder()
                .url("https://pokeapi.co/api/v2/type/" + typeName)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PokemonTypeLibraryActivity.this, "Failed to load Pokémon", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray pokemonArray = json.getJSONArray("pokemon");

                    if (pokemonArray.length() == 0) {
                        runOnUiThread(() -> {
                            showMessage("No Pokémon found for this type.");
                            pokemonAdapter.notifyDataSetChanged();
                        });
                        return;
                    }

                    for (int i = 0; i < pokemonArray.length(); i++) {
                        JSONObject pokeObj = pokemonArray.getJSONObject(i).getJSONObject("pokemon");
                        String name = pokeObj.getString("name");
                        String url = pokeObj.getString("url");
                        String[] parts = url.split("/");
                        String id = parts[parts.length - 1].isEmpty() ? parts[parts.length - 2] : parts[parts.length - 1];
                        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";

                        pokemonList.add(new Pokemon(name, id, imageUrl));
                    }

                    runOnUiThread(() -> {
                        messageTextView.setVisibility(View.GONE);
                        pokemonAdapter.notifyDataSetChanged();
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showMessage(String message) {
        messageTextView.setText(message);
        messageTextView.setVisibility(View.VISIBLE);
    }
}
