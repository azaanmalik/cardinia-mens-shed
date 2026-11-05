package com.example.cardiniamensshed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private final List<Product> productList;

    public ProductsAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descriptionText, priceText, quantityText;

        public ProductViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.product_name);
            descriptionText = view.findViewById(R.id.product_description);
            priceText = view.findViewById(R.id.product_price);
            quantityText = view.findViewById(R.id.product_quantity);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameText.setText(product.getName());
        holder.descriptionText.setText(product.getDescription());
        holder.priceText.setText("Price: $" + product.getPrice());
        holder.quantityText.setText("Quantity: " + product.getQuantity());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
