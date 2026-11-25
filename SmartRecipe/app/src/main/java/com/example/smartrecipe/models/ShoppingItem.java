package com.example.smartrecipe.models;

import com.google.gson.annotations.SerializedName;

public class ShoppingItem {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("quantity")
    private String quantity;

    @SerializedName("unit")
    private String unit;

    @SerializedName("status")
    private String status; // "pending" or "purchased"

    public ShoppingItem() {}

    public ShoppingItem(int userId, String itemName, String quantity, String unit, String status) {
        this.userId = userId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPurchased() {
        return "purchased".equalsIgnoreCase(status);
    }
}
