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
            case "normal": return Color.parseColor("#A8A77A");     // Soft Olive
            case "fire": return Color.parseColor("#EE8130");       // Warm Orange
            case "water": return Color.parseColor("#6390F0");      // Clear Blue
            case "electric": return Color.parseColor("#F7D02C");   // Soft Gold
            case "grass": return Color.parseColor("#7AC74C");      // Fresh Green
            case "ice": return Color.parseColor("#96D9D6");        // Cool Cyan
            case "fighting": return Color.parseColor("#C22E28");   // Brick Red
            case "poison": return Color.parseColor("#A33EA1");     // Vivid Purple
            case "ground": return Color.parseColor("#E2BF65");     // Sand
            case "flying": return Color.parseColor("#A98FF3");     // Sky Lavender
            case "psychic": return Color.parseColor("#F95587");    // Pink Rose
            case "bug": return Color.parseColor("#A6B91A");        // Avocado Green
            case "rock": return Color.parseColor("#B6A136");       // Amber Stone
            case "ghost": return Color.parseColor("#735797");      // Faded Indigo
            case "dragon": return Color.parseColor("#6F35FC");     // Royal Purple
            case "dark": return Color.parseColor("#705746");       // Deep Brown
            case "steel": return Color.parseColor("#B7B7CE");      // Metallic Lavender
            case "fairy": return Color.parseColor("#D685AD");      // Light Pink
            default: return ContextCompat.getColor(context, android.R.color.darker_gray);
        }
    }
}
