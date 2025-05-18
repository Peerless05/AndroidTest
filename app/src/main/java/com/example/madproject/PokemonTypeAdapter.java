package com.example.madproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PokemonTypeAdapter extends RecyclerView.Adapter<PokemonTypeAdapter.TypeViewHolder> {

    private final List<String> typeList;
    private final OnTypeClickListener listener;

    public interface OnTypeClickListener {
        void onTypeClick(String type);
    }

    public PokemonTypeAdapter(List<String> typeList, OnTypeClickListener listener) {
        this.typeList = typeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_type, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        String typeName = typeList.get(position);
        holder.typeText.setText(typeName.toUpperCase());
        holder.itemView.setBackgroundColor(getTypeColor(typeName, holder.itemView.getContext()));
        holder.itemView.setOnClickListener(v -> listener.onTypeClick(typeName));
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    static class TypeViewHolder extends RecyclerView.ViewHolder {
        TextView typeText;

        TypeViewHolder(View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.typeText);
        }
    }

    private int getTypeColor(String type, Context context) {
        switch (type.toLowerCase()) {
            case "fire": return Color.parseColor("#F08030");
            case "water": return Color.parseColor("#6890F0");
            case "grass": return Color.parseColor("#78C850");
            case "electric": return Color.parseColor("#F8D030");
            case "psychic": return Color.parseColor("#F85888");
            case "ice": return Color.parseColor("#98D8D8");
            case "dragon": return Color.parseColor("#7038F8");
            case "dark": return Color.parseColor("#705848");
            case "fairy": return Color.parseColor("#EE99AC");
            case "normal": return Color.parseColor("#A8A878");
            case "fighting": return Color.parseColor("#C03028");
            case "flying": return Color.parseColor("#A890F0");
            case "poison": return Color.parseColor("#A040A0");
            case "ground": return Color.parseColor("#E0C068");
            case "rock": return Color.parseColor("#B8A038");
            case "bug": return Color.parseColor("#A8B820");
            case "ghost": return Color.parseColor("#705898");
            case "steel": return Color.parseColor("#B8B8D0");
            default: return ContextCompat.getColor(context, android.R.color.darker_gray);
        }
    }
}
