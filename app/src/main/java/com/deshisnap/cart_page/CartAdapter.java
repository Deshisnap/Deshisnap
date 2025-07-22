package com.deshisnap.cart_page;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<SimpleCartItem> cartItems;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(int position);
    }

    public CartAdapter(List<SimpleCartItem> cartItems, OnItemDeleteListener deleteListener) {
        this.cartItems = cartItems;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_recycler_view, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        SimpleCartItem item = cartItems.get(position);
        holder.serviceNameTextView.setText(item.getServiceName());
        holder.servicePriceTextView.setText(item.getServicePrice()); // Display as is "Rs. 100" or "$20"

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView serviceNameTextView;
        TextView servicePriceTextView;
        ImageView deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameTextView = itemView.findViewById(R.id.item_service_name);
            servicePriceTextView = itemView.findViewById(R.id.item_service_price);
            deleteButton = itemView.findViewById(R.id.item_delete_button);
        }
    }
}
