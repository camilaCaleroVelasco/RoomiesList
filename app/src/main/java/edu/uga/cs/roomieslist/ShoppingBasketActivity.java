package edu.uga.cs.roomieslist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class ShoppingBasketActivity extends AppCompatActivity {

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

        // Retrieve group ID from the intent
        userGroupId = getIntent().getStringExtra("GROUP_ID");
        if (userGroupId == null || userGroupId.isEmpty()) {
            Toast.makeText(this, "Error: Group ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Initialize Firebase reference for the shopping basket
        basketReference = FirebaseDatabase.getInstance().getReference("ShoppingBasket").child(userGroupId);
        shoppingListReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);

        // Initialize RecyclerView and adapter
        shoppingBasketRecyclerView = findViewById(R.id.shoppingBasketRecyclerView);
        shoppingBasketRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        basketItems = new ArrayList<>();
        adapter = new ShoppingListAdapter(basketItems, new ShoppingListAdapter.OnItemClickListener() {
            @Override
            public void onItemEditClick(Item item) {
                Toast.makeText(ShoppingBasketActivity.this, "Editing basket items is not allowed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDeleteClick(Item item) {
                deleteItemMoveBackToList(item);
            }

            @Override
            public void updateItemInFirebase(Item item) {
                itemPurchaseStatus(item);
            }
        });
        shoppingBasketRecyclerView.setAdapter(adapter);

        // Load basket items
        loadBasketItems();

        // Handle Logout Button Click
        findViewById(R.id.logoutButton2).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out the user
            Intent intent = new Intent(ShoppingBasketActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close current activity
        });
    }

    private void loadBasketItems() {
        basketReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                basketItems.clear(); // Clear current list
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    if (item != null) {
                        basketItems.add(item); // Add to local list
                    }
                }
                adapter.notifyDataSetChanged(); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShoppingBasketActivity.this, "Failed to load basket items.", Toast.LENGTH_SHORT).show();
                Log.e("ShoppingBasketActivity", "Error loading basket items: " + error.getMessage());
            }
        });
    }

    private void deleteItemMoveBackToList(Item item) {
        // Remove the item from the basket
        basketReference.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Reset the item properties for moving back to the shopping list
                item.setPurchased(false);
                item.setPurchasedBy(null);
                item.setSelected(false);
                item.setPrice(0.0);

                // Add the item back to the shopping list
                shoppingListReference.child(item.getItemId()).setValue(item).addOnCompleteListener(addTask -> {
                    if (addTask.isSuccessful()) {
                        basketItems.remove(item);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Item moved back to shopping list", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ShoppingBasketActivity", "Failed to add item back to shopping list.");
                        Toast.makeText(this, "Failed to move item to shopping list", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("ShoppingBasketActivity", "Failed to remove item from basket.");
                Toast.makeText(this, "Failed to remove item from basket", Toast.LENGTH_SHORT).show();            }
        });
    }

    private void itemPurchaseStatus(Item item) {
        if (item.isPurchased()) {
            // Mark as purchased: Move to basket and remove from shopping list
            basketReference.child(item.getItemId()).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    shoppingListReference.child(item.getItemId()).removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            loadBasketItems(); // Refresh basket
                        }
                    });
                }
            });
        } else {
            // Unmark as purchased: Move back to shopping list and remove from basket
            shoppingListReference.child(item.getItemId()).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    basketReference.child(item.getItemId()).removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            loadBasketItems(); // Refresh basket
                        }
                    });
                }
            });
        }
    }


}