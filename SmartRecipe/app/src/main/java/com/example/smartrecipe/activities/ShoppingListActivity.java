package com.example.smartrecipe.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipe.R;
import com.example.smartrecipe.adapters.ShoppingListAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.ShoppingItem;
import com.example.smartrecipe.utils.Constants;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.SessionManager;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingListActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SessionManager session;
    private final List<ShoppingItem> items = new ArrayList<>();
    private ShoppingListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        session = new SessionManager(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvShoppingList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShoppingListAdapter(this, items, new ShoppingListAdapter.OnShoppingListener() {
            @Override
            public void onTogglePurchased(ShoppingItem item, boolean isPurchased) {
                updateItemStatus(item, isPurchased ? "purchased" : "pending");
            }

            @Override
            public void onEdit(ShoppingItem item) {
                showItemDialog(item);
            }

            @Override
            public void onDelete(ShoppingItem item, int position) {
                deleteItem(item, position);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAddItem).setOnClickListener(v -> showItemDialog(null));
        loadShoppingList();
    }

    private void loadShoppingList() {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getShoppingList(session.getUserId())
                .enqueue(new Callback<ApiResponse<List<ShoppingItem>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ShoppingItem>>> call,
                                           Response<ApiResponse<List<ShoppingItem>>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            items.clear();
                            items.addAll(response.body().getData());
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ShoppingItem>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ShoppingListActivity.this, "Failed to load shopping list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showItemDialog(ShoppingItem existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etName = new EditText(this);
        etName.setHint("Item Name (e.g. Tomatoes)");
        EditText etQty = new EditText(this);
        etQty.setHint("Quantity (e.g. 500)");
        Spinner spUnit = new Spinner(this);
        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Constants.SHOPPING_UNITS));

        if (existing != null) {
            etName.setText(existing.getItemName());
            etQty.setText(existing.getQuantity());
            for (int i = 0; i < Constants.SHOPPING_UNITS.length; i++) {
                if (Constants.SHOPPING_UNITS[i].equalsIgnoreCase(existing.getUnit())) {
                    spUnit.setSelection(i);
                    break;
                }
            }
        }

        layout.addView(etName);
        layout.addView(etQty);
        layout.addView(spUnit);

        new AlertDialog.Builder(this)
                .setTitle(existing != null ? "Edit Shopping Item" : "Add Shopping Item")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String qty = etQty.getText().toString().trim();
                    String unit = spUnit.getSelectedItem().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("user_id", session.getUserId());
                    body.put("item_name", name);
                    body.put("quantity", qty.isEmpty() ? "1" : qty);
                    body.put("unit", unit);
                    body.put("status", existing != null ? existing.getStatus() : "pending");

                    Call<ApiResponse<ShoppingItem>> call = existing != null
                            ? RetrofitClient.getApiService(this).updateShoppingItem(existing.getId(), body)
                            : RetrofitClient.getApiService(this).addShoppingItem(body);

                    call.enqueue(new Callback<ApiResponse<ShoppingItem>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ShoppingItem>> call,
                                               Response<ApiResponse<ShoppingItem>> response) {
                            if (response.isSuccessful()) {
                                loadShoppingList();
                            } else {
                                Toast.makeText(ShoppingListActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ShoppingItem>> call, Throwable t) {
                            Toast.makeText(ShoppingListActivity.this, "Error saving item", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateItemStatus(ShoppingItem item, String newStatus) {
        item.setStatus(newStatus);
        Map<String, Object> body = new HashMap<>();
        body.put("item_name", item.getItemName());
        body.put("quantity", item.getQuantity());
        body.put("unit", item.getUnit());
        body.put("status", newStatus);

        RetrofitClient.getApiService(this).updateShoppingItem(item.getId(), body)
                .enqueue(new Callback<ApiResponse<ShoppingItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ShoppingItem>> call, Response<ApiResponse<ShoppingItem>> response) {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ShoppingItem>> call, Throwable t) {}
                });
    }

    private void deleteItem(ShoppingItem item, int position) {
        RetrofitClient.getApiService(this).deleteShoppingItem(item.getId())
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful()) {
                            items.remove(position);
                            adapter.notifyItemRemoved(position);
                            tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                });
    }
}
