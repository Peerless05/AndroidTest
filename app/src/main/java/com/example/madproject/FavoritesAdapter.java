package com.example.madproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {

    private final List<Pokemon> favoriteList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Pokemon pokemon);
        void onRemoveClick(Pokemon pokemon);
    }

    public FavoritesAdapter(Context context, List<Pokemon> favorites, OnItemClickListener listener) {
        this.context = context;
        this.favoriteList = favorites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_pokemon, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        Pokemon pokemon = favoriteList.get(position);

        holder.pokemonName.setText(capitalize(pokemon.getName()));
        holder.pokemonId.setText("#" + pokemon.getId());
        holder.pokemonTypes.setText(joinTypes(pokemon.getType()));

        Glide.with(context)
                .load(pokemon.getImageUrl())
                .placeholder(R.drawable.pokeballgif)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.pokemonImage);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(pokemon));
        holder.removeButton.setOnClickListener(v -> listener.onRemoveClick(pokemon));
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView pokemonImage;
        TextView pokemonName, pokemonId, pokemonTypes;
        ImageButton removeButton;

        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            pokemonImage = itemView.findViewById(R.id.pokemonImage);
            pokemonName = itemView.findViewById(R.id.pokemonName);
            pokemonId = itemView.findViewById(R.id.pokemonId);
            pokemonTypes = itemView.findViewById(R.id.pokemonTypes);
            removeButton = itemView.findViewById(R.id.removeFavoriteButton);
        }
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String joinTypes(List<String> types) {
        if (types == null || types.isEmpty()) return "Unknown";
        return String.join(", ", types);
    }
}
