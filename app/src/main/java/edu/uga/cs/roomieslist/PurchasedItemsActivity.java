package edu.uga.cs.roomieslist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchasedItemsActivity extends AppCompatActivity {

    private RecyclerView purchasedItemsRecyclerView;
    private PurchasedItemsAdapter adapter;
    private DatabaseReference purchasedItemsReference;
    private DatabaseReference shoppingListReference;
    private List<PurchasedRecord> purchasedRecords;
    private String userGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_items);

        // Initialize Firebase references
        userGroupId = getIntent().getStringExtra("GROUP_ID");
        purchasedItemsReference = FirebaseDatabase.getInstance().getReference("PurchasedItems").child(userGroupId);
        shoppingListReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);

        // Initialize RecyclerView
        purchasedItemsRecyclerView = findViewById(R.id.purchasedItemsRecyclerView);
        purchasedItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        purchasedRecords = new ArrayList<>();
        adapter = new PurchasedItemsAdapter(purchasedRecords, userGroupId, new PurchasedItemsAdapter.OnItemClickListener() {
            @Override
            public void onUpdatePriceClick(PurchasedRecord record, double newPrice) {
                updatePurchasePrice(record, newPrice);
            }
        });
        purchasedItemsRecyclerView.setAdapter(adapter);

        // Load purchased items
        loadPurchasedItems();

        // Settle Costs button
        Button settleCostsButton = findViewById(R.id.settleCostsButton);
        settleCostsButton.setOnClickListener(v -> settleCosts());

        // Back to Shopping List button
        Button backToShoppingListButton = findViewById(R.id.backToShoppingListButton);
        backToShoppingListButton.setOnClickListener(v -> {
            Intent intent = new Intent(PurchasedItemsActivity.this, ShoppingListActivity.class);
            intent.putExtra("GROUP_ID", userGroupId); // Pass group ID to Shopping List
            startActivity(intent);
            finish(); // Close current activity
        });

        // Handle Logout Button Click
        findViewById(R.id.logoutButton3).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out the user
            Intent intent = new Intent(PurchasedItemsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close current activity
        });
    }

    private void loadPurchasedItems() {
        purchasedItemsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                purchasedRecords.clear();
                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    PurchasedRecord record = recordSnapshot.getValue(PurchasedRecord.class);
                    if (record != null) {
                        record.setId(recordSnapshot.getKey());
                        purchasedRecords.add(record);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PurchasedItemsActivity.this, "Failed to load purchased items.", Toast.LENGTH_SHORT).show();
                Log.e("PurchasedItemsActivity", "Error: " + error.getMessage());
            }
        });
    }

    private void updatePurchasePrice(PurchasedRecord record, double newPrice) {
        purchasedItemsReference.child(record.getId()).child("totalPrice").setValue(newPrice).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                record.setTotalPrice(newPrice);
                adapter.notifyDataSetChanged();
                Toast.makeText(PurchasedItemsActivity.this, "Purchase price updated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PurchasedItemsActivity.this, "Failed to update purchase price.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void settleCosts() {
        if (purchasedRecords.isEmpty()) {
            Toast.makeText(this, "No purchases to settle!", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalCost = 0.0;
        Map<String, Double> roommateSpending = new HashMap<>();

        // Calculate total cost and spending per roommate
        for (PurchasedRecord record : purchasedRecords) {
            totalCost += record.getTotalPrice();

            String roommate = record.getPurchasedBy();
            roommateSpending.put(roommate, roommateSpending.getOrDefault(roommate, 0.0) + record.getTotalPrice());
        }

        // Calculate average spending
        int roommateCount = roommateSpending.size();
        double averageSpending = totalCost / roommateCount;

        // Compute differences for each roommate
        StringBuilder results = new StringBuilder("Settlement Results:\n\n");
        for (Map.Entry<String, Double> entry : roommateSpending.entrySet()) {
            String roommate = entry.getKey();
            double spent = entry.getValue();
            double difference = spent - averageSpending;

            results.append(String.format("%s:\n  Spent: $%.2f\n  Difference: $%.2f\n\n",
                    roommate, spent, difference));
        }

        results.append(String.format("Total Spent: $%.2f\nAverage Spent: $%.2f\n", totalCost, averageSpending));

        // Show results in a dialog
        new AlertDialog.Builder(this)
                .setTitle("Settle Costs")
                .setMessage(results.toString())
                .setPositiveButton("OK", (dialog, which) -> clearPurchasedItems())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearPurchasedItems() {
        purchasedItemsReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                purchasedRecords.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "All purchases cleared!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to clear purchases.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
