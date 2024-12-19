package com.example.byte_benders_accessibility_app;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.CardViewHolder> {

    private String[] cardContents; // Data for each card
    private final Context context;

    public SettingsAdapter(Context context, String[] cardContents) {
        this.context = context;
        this.cardContents = cardContents;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.setting_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // Set the content for each CardView
        holder.bind(cardContents[position]);
    }

    @Override
    public int getItemCount() {
        return cardContents.length;
    }

    public void updateData(String[] newCardContents) {
        this.cardContents = newCardContents;
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(String content) {
            // Use Html.fromHtml() to render HTML tags properly
            TextView textView = cardView.findViewById(R.id.textView);
            textView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        }
    }
}
