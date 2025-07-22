package com.deshisnap.cart_page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.R;

import java.util.List;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.OfferViewHolder> {

    private List<Offer> offers;
    private OnOfferApplyListener applyListener;

    public interface OnOfferApplyListener {
        void onOfferApply(Offer offer);
    }

    public OffersAdapter(List<Offer> offers, OnOfferApplyListener applyListener) {
        this.offers = offers;
        this.applyListener = applyListener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.descriptionTextView.setText(offer.getDescription());
        holder.codeTextView.setText("Code: " + offer.getCode());

        holder.applyButton.setOnClickListener(v -> {
            if (applyListener != null) {
                applyListener.onOfferApply(offer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView descriptionTextView;
        TextView codeTextView;
        Button applyButton;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.offer_description_text);
            codeTextView = itemView.findViewById(R.id.offer_code_text);
            applyButton = itemView.findViewById(R.id.offer_apply_button);
        }
    }
}