package edu.uga.cs.roomieslist;

public class Item {
    public String itemId;
    public String name;
    public boolean purchased;
    public double price;
    public String purchasedBy;
    private boolean isSelected;
    private String addedBy;
    private String groupId;
    private int amount;

    public Item(){

    }

    public Item(String itemId, String name, double price, String purchasedBy, String addedBy, String groupId, int amount) {
        this.itemId = itemId;
        this.name = name;
        this.purchased = false;
        this.price = price;
        this.purchasedBy = purchasedBy;
        this.isSelected = false;
        this.addedBy = addedBy;
        this.groupId = groupId;
        this.amount = amount;

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
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public boolean isPurchased() {
        return purchased;
    }
    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getPurchasedBy() {
        return purchasedBy;
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


    public String getAddedBy() {
        return addedBy;
    }
    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getAmount(){
        return amount;
    }
    public void setAmount(int amount){
        this.amount = amount;
    }
}
