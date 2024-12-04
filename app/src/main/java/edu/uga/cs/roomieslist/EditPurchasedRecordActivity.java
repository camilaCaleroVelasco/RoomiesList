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

public class EditPurchasedRecordActivity extends AppCompatActivity {

    private RecyclerView editPurchasedRecyclerView;
    private ShoppingListAdapter adapter;
    private DatabaseReference shoppingListReference;
    private DatabaseReference purchasedItemsReference;
    private List<Item> purchasedItems;
    private PurchasedRecord purchasedRecord;
    private String userGroupId;
    private String purchaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_purchased_record);

        userGroupId = getIntent().getStringExtra("GROUP_ID");
        purchaseId = getIntent().getStringExtra("PURCHASE_ID");

        purchasedItemsReference = FirebaseDatabase.getInstance()
                .getReference("PurchasedItems")
                .child(userGroupId)
                .child(purchaseId);

        shoppingListReference = FirebaseDatabase.getInstance()
                .getReference("ShoppingList")
                .child(userGroupId);

        editPurchasedRecyclerView = findViewById(R.id.editPurchasedRecyclerView);
        editPurchasedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        purchasedItems = new ArrayList<>();
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
                item.setPurchased(!item.isPurchased());
                if (!item.isPurchased()) {
                    moveItemBackToShoppingList(item);
                }
            }
        });

        editPurchasedRecyclerView.setAdapter(adapter);

        loadPurchasedItems();

        Button saveChangesButton = findViewById(R.id.saveChangesButton);
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

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