package edu.uga.cs.roomieslist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * This Activity edits the purchased records, which allows users to move back items to the
 * shopping list and save the changes in the records
 */
public class EditPurchasedRecordActivity extends AppCompatActivity {

    // Variables
    private RecyclerView editPurchasedRecyclerView;
    private ShoppingListAdapter adapter;
    private DatabaseReference shoppingListReference;
    private DatabaseReference purchasedItemsReference;
    private List<Item> purchasedItems;
    private PurchasedRecord purchasedRecord;
    private String userGroupId;
    private String purchaseId;

    /**
     * Initializes the Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchased_record);

        // Get Group_ID and PURCHASE_ID
        userGroupId = getIntent().getStringExtra("GROUP_ID");
        purchaseId = getIntent().getStringExtra("PURCHASE_ID");

        // Initialize Firebase Database references
        purchasedItemsReference = FirebaseDatabase.getInstance()
                .getReference("PurchasedItems")
                .child(userGroupId)
                .child(purchaseId);
        shoppingListReference = FirebaseDatabase.getInstance()
                .getReference("ShoppingList")
                .child(userGroupId);

        // Obtain the Recycler View object
        editPurchasedRecyclerView = findViewById(R.id.editPurchasedRecyclerView);
        editPurchasedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start purchased items list
        purchasedItems = new ArrayList<>();
        // Initialize adapter
        adapter = new ShoppingListAdapter(purchasedItems, new ShoppingListAdapter.OnItemClickListener() {
            @Override
            public void onItemEditClick(Item item) {
                Toast.makeText(EditPurchasedRecordActivity.this, "Editing is not allowed here.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDeleteClick(Item item) {
                moveItemBackToShoppingList(item);
            }

            @Override
            public void updateItemInFirebase(Item item) {
                // Move the item if item is not purchased
                item.setPurchased(!item.isPurchased());
                if (!item.isPurchased()) {
                    moveItemBackToShoppingList(item);
                }
            }
        });

        // Attach to the adapter
        editPurchasedRecyclerView.setAdapter(adapter);

        // Get purchased items
        loadPurchasedItems();

        // Save changes button
        Button saveChangesButton = findViewById(R.id.saveChangesButton);
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    /**
     * Get the current items that are market at purchased in the Firebase database
     */
    private void loadPurchasedItems() {
        purchasedItemsReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                purchasedRecord = snapshot.getValue(PurchasedRecord.class);
                if (purchasedRecord != null) {
                    purchasedItems.clear();
                    purchasedItems.addAll(purchasedRecord.getItems());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPurchasedRecordActivity.this, "Failed to load purchased items.", Toast.LENGTH_SHORT).show();
                Log.e("EditPurchasedRecord", "Error: " + error.getMessage());
            }
        });
    }

    /**
     * If purchased item is unmarked, then move it back to the shopping list
     * and remove it from the purchased list
     * @param item
     */
    private void moveItemBackToShoppingList(Item item) {
        item.setPurchased(false);
        shoppingListReference.child(item.getItemId()).setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                purchasedItems.remove(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(EditPurchasedRecordActivity.this, "Item moved back to shopping list.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditPurchasedRecordActivity.this, "Failed to move item.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Saves the updated records and recalculate the prices
    private void saveChanges() {
        purchasedRecord.setItems(new ArrayList<>(purchasedItems));
        double newTotalPrice = 0.0;
        for (Item item : purchasedItems) {
            newTotalPrice += item.getPrice();
        }
        purchasedRecord.setTotalPrice(newTotalPrice);

        purchasedItemsReference.setValue(purchasedRecord).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditPurchasedRecordActivity.this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditPurchasedRecordActivity.this, PurchasedItemsActivity.class);
                intent.putExtra("GROUP_ID", userGroupId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(EditPurchasedRecordActivity.this, "Failed to save changes.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}