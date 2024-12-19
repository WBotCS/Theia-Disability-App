// CardAdapter.java
package com.example.byte_benders_accessibility_app;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Building> buildings;
    private Context context;

    public CardAdapter(Context context, List<Building> buildings) {
        this.context = context;
        this.buildings = buildings;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Building building = buildings.get(position);
        holder.buildingName.setText(building.getName());
        holder.buildingDistance.setText(building.getDistance());
        holder.buildingTime.setText(building.getWalkingTime());

        GestureDetector gestureDetector = new GestureDetector(context, new CustomGestureListener(building));

        holder.itemView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public int getItemCount() {
        return buildings.size();
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {
        private Building building;

        CustomGestureListener(Building building) {
            this.building = building;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Trigger TTS with all information
            if (context instanceof NavigationPage) {
                String textToSpeak = building.getName() + ", Distance: " + building.getDistance() + " yards, " + building.getWalkingTime();
                ((NavigationPage) context).speak(textToSpeak);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Trigger TTS with all information and additional question
            if (context instanceof NavigationPage) {
                String textToSpeak = building.getName() + ", Distance: " + building.getDistance() + " yards, " + building.getWalkingTime() + ". Would you like to go there?";
                ((NavigationPage) context).speak(textToSpeak);
            }
            return true;
        }

//        @Override
//        public void onLongPress(MotionEvent e) {
//            // Trigger TTS to start route
//            if (context instanceof NavigationPage) {
//                ((NavigationPage) context).speak("Start route to " + building.getName());
//            }
//        }
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView buildingName;
        TextView buildingDistance;
        TextView buildingTime;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            buildingName = itemView.findViewById(R.id.buildingName);
            buildingDistance = itemView.findViewById(R.id.buildingDistance);
            buildingTime = itemView.findViewById(R.id.buildingTime);
        }
    }
}