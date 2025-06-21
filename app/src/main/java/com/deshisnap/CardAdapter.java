package com.foodcafe.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardItem> cardList;

    public CardAdapter(List<CardItem> cardList) {
        this.cardList = cardList;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public CardViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardImage);
            textView = itemView.findViewById(R.id.cardText);
        }
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_with_image, parent, false);

        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        CardItem item = cardList.get(position);
        holder.imageView.setImageResource(item.getImageRes());
        holder.textView.setText(item.getText());
        Utils.applyGradientToText(holder.textView, "#04FDAA", "#01D3F8");
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }


}

