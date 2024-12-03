package edu.uga.cs.roomieslist;

import java.util.List;

public class PurchasedRecord {
    private String id; // Add this field
    private String purchasedBy;
    private List<Item> items;
    private double totalPrice;
    private long timestamp;

    public PurchasedRecord() {}

    public PurchasedRecord(String purchasedBy, List<Item> items, double totalPrice, long timestamp) {
        this.purchasedBy = purchasedBy;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    // Getter and Setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Existing getters and setters
    public String getPurchasedBy() {
        return purchasedBy;
    }

    public void setPurchasedBy(String purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}