package com.example.smartrecipe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;

import java.util.List;

public class AdminRowAdapter<T> extends RecyclerView.Adapter<AdminRowAdapter.ViewHolder> {

    public interface RowBinder<T> {
        String getPrimaryText(T item);
        String getSecondaryText(T item);
        boolean allowEdit(T item);
        void onEdit(T item);
        void onDelete(T item, int position);
    }

    private final Context context;
    private final List<T> items;
    private final RowBinder<T> binder;

    public AdminRowAdapter(Context context, List<T> items, RowBinder<T> binder) {
        this.context = context;
        this.items = items;
        this.binder = binder;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        holder.tvPrimary.setText(binder.getPrimaryText(item));
        holder.tvSecondary.setText(binder.getSecondaryText(item));
        holder.btnEdit.setVisibility(binder.allowEdit(item) ? View.VISIBLE : View.GONE);
        holder.btnEdit.setOnClickListener(v -> binder.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> binder.onDelete(item, holder.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrimary, tvSecondary;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrimary = itemView.findViewById(R.id.tvPrimaryText);
            tvSecondary = itemView.findViewById(R.id.tvSecondaryText);
            btnEdit = itemView.findViewById(R.id.btnAdminEdit);
            btnDelete = itemView.findViewById(R.id.btnAdminDelete);
        }
    }
}
