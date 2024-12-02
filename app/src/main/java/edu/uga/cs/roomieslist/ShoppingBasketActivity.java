package edu.uga.cs.roomieslist;

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
                deleteItemFromBasket(item);
            }

            @Override
            public void updateItemInFirebase(Item item) {
                Toast.makeText(ShoppingBasketActivity.this, "Updating basket items is not allowed.", Toast.LENGTH_SHORT).show();
            }
        });
        shoppingBasketRecyclerView.setAdapter(adapter);

        // Load basket items
        loadBasketItems();
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

    private void deleteItemFromBasket(Item item) {
        basketReference.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                basketItems.remove(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Item removed from basket", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        });
    }

}