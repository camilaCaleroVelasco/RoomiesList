package edu.uga.cs.roomieslist;

import java.util.List;

/**
 * POJO class
 */
public class PurchasedRecord {
    private String id;
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

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

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