package edu.uga.cs.roomieslist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the basket Activity.
 * It allows the users to view, move items back to the shopping list and go to checkout.
 */
public class ShoppingBasketActivity extends AppCompatActivity {

    // Variables
    private static final String DEBUG_TAG = "ShoppingBasketActivity";
    private RecyclerView shoppingBasketRecyclerView;
    private ShoppingListAdapter adapter;
    private DatabaseReference basketReference;
    private String userGroupId;
    private List<Item> basketItems;
    private DatabaseReference shoppingListReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_basket);

        // Get user group id from profile when user first gets to the list
        userGroupId = getIntent().getStringExtra("GROUP_ID");
        if (userGroupId == null || userGroupId.isEmpty()) {
            Toast.makeText(this, "Error: Group ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create the Firebase reference for the shopping basket
        basketReference = FirebaseDatabase.getInstance().getReference("ShoppingBasket").child(userGroupId);

        // Create a reference to the Firebase for shopping list
        shoppingListReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);

        // Obtain object View
        shoppingBasketRecyclerView = findViewById(R.id.shoppingBasketRecyclerView);
        shoppingBasketRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Array of items in basket initialize
        basketItems = new ArrayList<>();

        // Use adapter
        adapter = new ShoppingListAdapter(basketItems, new ShoppingListAdapter.OnItemClickListener() {

            /**
             * Item will not be edited in basket Activity
             * @param item
             */
            @Override
            public void onItemEditClick(Item item) {
                Toast.makeText(ShoppingBasketActivity.this, "Not allowed to edit item", Toast.LENGTH_SHORT).show();
            }

            /**
             * Item will be deleted from the basket list and added back to the shopping list
             * @param item
             */
            @Override
            public void onItemDeleteClick(Item item) {
                deleteItemMoveBackToList(item);
            }

            /**
             * Item can be unmarked and moved back to the shopping list
             * @param item
             */
            @Override
            public void updateItemInFirebase(Item item) {
                itemPurchaseStatus(item);
            }
        });
        shoppingBasketRecyclerView.setAdapter(adapter);

        // Get basket list items
        loadBasketItems();

        // Handle Logout Button Click
        findViewById(R.id.logoutButton2).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out the user
            Intent intent = new Intent(ShoppingBasketActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Navigate user to purchase history when Checkout Button is clicked
        Button checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> checkoutItems());

        // Handle View Past Purchases Button Click
        Button viewPastPurchasesButton = findViewById(R.id.viewPastPurchasesButton);
        viewPastPurchasesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingBasketActivity.this, PurchasedItemsActivity.class);
            intent.putExtra("GROUP_ID", userGroupId);
            startActivity(intent);
        });
    }

    /**
     * Get the items from the basket from Firebase
     */
    private void loadBasketItems() {
        basketReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                basketItems.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    if (item != null) {
                        basketItems.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShoppingBasketActivity.this, "Failed to load basket items.", Toast.LENGTH_SHORT).show();
                Log.e(DEBUG_TAG, "Error loading basket items: " + error.getMessage());
            }
        });
    }

    /**
     * Remove the item from basket list if the item is unpurchased and move it back to the shopping list
     * @param item
     */
    private void deleteItemMoveBackToList(Item item) {
        // Remove the item from the basket list
        basketReference.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Set the values back to default
                item.setPurchased(false);
                item.setPurchasedBy(null);
                item.setSelected(false);
                item.setPrice(0.0);

                // Send the item back to the shopping list
                shoppingListReference.child(item.getItemId()).setValue(item).addOnCompleteListener(addTask -> {
                    if (addTask.isSuccessful()) {
                        basketItems.remove(item);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Item moved back to shopping list", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(DEBUG_TAG, "Failed to add item back to shopping list.");
                        Toast.makeText(this, "Failed to move item to shopping list", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(DEBUG_TAG, "Failed to remove item from basket.");
                Toast.makeText(this, "Failed to remove item from basket", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the items purchased status if the user unmarks or marks the item
     * @param item
     */
    private void itemPurchaseStatus(Item item) {
        if (item.isPurchased()) {
            // If item is purchased, mark as purchased
            // Then move the item to the basket list
            basketReference.child(item.getItemId()).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    shoppingListReference.child(item.getItemId()).removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            loadBasketItems();
                        }
                    });
                }
            });
        } else {
            // If item is unpurchased, unmark as purchased
            // Then move the item back to the shopping list and remove from basket
            item.setPurchasedBy(null);
            shoppingListReference.child(item.getItemId()).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    basketReference.child(item.getItemId()).removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            loadBasketItems();
                        }
                    });
                }
            });
        }
    }

    /**
     * Handles the checkout process
     */
    private void checkoutItems() {
        if (basketItems.isEmpty()) {
            Toast.makeText(this, "Your basket is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the current user's name
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = "Unknown User";
        }

        // Calculate total price
        double totalPrice = 0.0;
        for (Item item : basketItems) {
            totalPrice += item.getPrice();
        }

        // Generate unique ID for the purchase record
        String purchaseId = FirebaseDatabase.getInstance()
                .getReference("PurchasedItems")
                .child(userGroupId)
                .push().getKey();

        if (purchaseId == null) {
            Toast.makeText(this, "Failed to generate purchase ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the purchased record
        PurchasedRecord record = new PurchasedRecord(
                userName, // purchasedBy
                new ArrayList<>(basketItems), // items
                totalPrice, // totalPrice
                System.currentTimeMillis() // timestamp
        );

        // Reference to the purchased items node
        DatabaseReference purchasedItemsRef = FirebaseDatabase.getInstance()
                .getReference("PurchasedItems")
                .child(userGroupId)
                .child(purchaseId);

        // Save the record to Firebase
        purchasedItemsRef.setValue(record).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Clear the basket after successful checkout
                basketReference.removeValue().addOnCompleteListener(clearTask -> {
                    if (clearTask.isSuccessful()) {
                        basketItems.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Checkout successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to the Purchased Items Page
                        Intent intent = new Intent(ShoppingBasketActivity.this, PurchasedItemsActivity.class);
                        intent.putExtra("GROUP_ID", userGroupId);
                        startActivity(intent);
                        finish(); // Close the basket page
                    } else {
                        Toast.makeText(this, "Failed to clear basket.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Checkout failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
