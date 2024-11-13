package edu.uga.cs.roomieslist;

public class Item {
    public String itemId;
    public String name;
    public boolean purchased;
    public double price;
    public String purchasedBy;
    private boolean isSelected;

    public Item(){

    }

    public Item(String itemId, String name){
        this.itemId = itemId;
        this.name = name;
        this.purchased = false;
        this.price = 0.0;
        this.purchasedBy = "";
        this.isSelected = false;
    }

    public Item(String itemId, String name, boolean purchased, double price, String purchasedBy) {
        this.itemId = itemId;
        this.name = name;
        this.purchased = purchased;
        this.price = price;
        this.purchasedBy = purchasedBy;
        this.isSelected = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getItemId() {
        return this.itemId;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPurchasedBy(String purchasedBy) {
        this.purchasedBy = purchasedBy;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }
}
