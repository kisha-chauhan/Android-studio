package com.example.smartrecipe.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.models.ShoppingItem;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    public interface OnShoppingListener {
        void onTogglePurchased(ShoppingItem item, boolean isPurchased);
        void onEdit(ShoppingItem item);
        void onDelete(ShoppingItem item, int position);
    }

    private final Context context;
    private final List<ShoppingItem> items;
    private final OnShoppingListener listener;

    public ShoppingListAdapter(Context context, List<ShoppingItem> items, OnShoppingListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);
        holder.tvName.setText(item.getItemName());
        String qty = (item.getQuantity() != null ? item.getQuantity() : "") + " " + (item.getUnit() != null ? item.getUnit() : "");
        holder.tvQty.setText(qty.trim());

        holder.cbPurchased.setOnCheckedChangeListener(null);
        boolean purchased = item.isPurchased();
        holder.cbPurchased.setChecked(purchased);

        if (purchased) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setAlpha(0.5f);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvName.setAlpha(1.0f);
        }

        holder.cbPurchased.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onTogglePurchased(item, isChecked);
        });
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbPurchased;
        TextView tvName, tvQty;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbPurchased = itemView.findViewById(R.id.cbPurchased);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvItemQty);
            btnEdit = itemView.findViewById(R.id.btnEditItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
