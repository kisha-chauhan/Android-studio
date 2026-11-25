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
import com.example.smartrecipe.adapters.AdminRowAdapter;
import com.example.smartrecipe.api.ApiResponse;
import com.example.smartrecipe.api.RetrofitClient;
import com.example.smartrecipe.models.User;
import com.example.smartrecipe.utils.NetworkUtils;
import com.example.smartrecipe.utils.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageUsersActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<User> users = new ArrayList<>();
    private AdminRowAdapter<User> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Users");
        toolbar.setNavigationOnClickListener(v -> finish());
        UiUtils.applyToolbarInsets(toolbar);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvAdminList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRowAdapter<>(this, users, new AdminRowAdapter.RowBinder<User>() {
            @Override
            public String getPrimaryText(User item) {
                return item.getName() + " (" + item.getRole() + ")";
            }

            @Override
            public String getSecondaryText(User item) {
                return item.getEmail() + " • " + item.getPhone();
            }

            @Override
            public boolean allowEdit(User item) {
                return true;
            }

            @Override
            public void onEdit(User item) {
                showUserDialog(item);
            }

            @Override
            public void onDelete(User item, int position) {
                deleteUser(item, position);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAdminAdd).setOnClickListener(v -> showUserDialog(null));
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    users.clear();
                    users.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(User existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        EditText etName = new EditText(this);
        etName.setHint("Full Name");
        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        EditText etPassword = new EditText(this);
        etPassword.setHint(existing == null ? "Password (min 6)" : "Leave blank to keep password");

        Spinner spRole = new Spinner(this);
        String[] roles = {"user", "admin"};
        spRole.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));

        if (existing != null) {
            etName.setText(existing.getName());
            etEmail.setText(existing.getEmail());
            etEmail.setEnabled(false);
            etPhone.setText(existing.getPhone());
            spRole.setSelection("admin".equalsIgnoreCase(existing.getRole()) ? 1 : 0);
        }

        layout.addView(etName);
        layout.addView(etEmail);
        layout.addView(etPhone);
        if (existing == null) layout.addView(etPassword);
        layout.addView(spRole);

        new AlertDialog.Builder(this)
                .setTitle(existing != null ? "Edit User" : "Add User")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("name", etName.getText().toString().trim());
                    body.put("email", etEmail.getText().toString().trim());
                    body.put("phone", etPhone.getText().toString().trim());
                    body.put("role", spRole.getSelectedItem().toString());
                    if (existing == null) {
                        body.put("password", etPassword.getText().toString());
                    }

                    Call<ApiResponse<User>> call = existing != null
                            ? RetrofitClient.getApiService(this).updateUser(existing.getId(), body)
                            : RetrofitClient.getApiService(this).adminCreateUser(body);

                    call.enqueue(new Callback<ApiResponse<User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                            if (response.isSuccessful()) {
                                loadUsers();
                            } else {
                                Toast.makeText(AdminManageUsersActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<User>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Delete user " + user.getName() + "?")
                .setPositiveButton("Delete", (d, w) ->
                        RetrofitClient.getApiService(this).deleteUser(user.getId())
                                .enqueue(new Callback<ApiResponse<Object>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                        if (response.isSuccessful()) {
                                            users.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        } else {
                                            Toast.makeText(AdminManageUsersActivity.this, NetworkUtils.parseError(response), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
