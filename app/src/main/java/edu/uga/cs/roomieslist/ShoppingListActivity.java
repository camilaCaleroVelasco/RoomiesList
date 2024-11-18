package edu.uga.cs.roomieslist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView shoppingListRecyclerView;
    private ShoppingListAdapter adapter;
    private DatabaseReference databaseReference;
    private String userGroupId;
    private List<Item> shoppingList;
    private String userName = "Unknown User";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);

        // Assume `userGroupId` is retrieved from the user's profile upon login or signup
        userGroupId = getIntent().getStringExtra("GROUP_ID");

        // Reference to this group's shopping list in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);

        // Initialize UI elements
        shoppingListRecyclerView = findViewById(R.id.shoppingListRecyclerView);
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shoppingList = new ArrayList<>();
        adapter = new ShoppingListAdapter(shoppingList, new ShoppingListAdapter.OnItemClickListener() {
            @Override
            public void onItemEditClick(Item item) {
                showEditItemDialog(item);
            }

            @Override
            public void onItemDeleteClick(Item item) {
                deleteItem(item);
            }

            @Override
            public void updateItemInFirebase(Item item) {
                showMarkPurchasedDialog(item);
            }
        });
        shoppingListRecyclerView.setAdapter(adapter);

        // Load shopping list for this group
        loadShoppingList();

        // Fetch the user's name when activity created
        getNameFromFireabse();

        // Button for adding new items
        findViewById(R.id.addItemButton).setOnClickListener(v -> showAddItemDialog());

        // Button for marking selected items as purchased
        findViewById(R.id.markPurchasedButton).setOnClickListener(v -> markItemsAsPurchased());
    }

    private void loadShoppingList() {
        DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    shoppingList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShoppingListActivity.this, "Failed to load shopping list.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddItemDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        final EditText input = new EditText(this);
        input.setHint("Item Name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = input.getText().toString().trim();
            if (!itemName.isEmpty()) {
                DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);
                String itemId = groupReference.push().getKey();
                // Use the stored userName directly
                Item newItem = new Item(itemId, itemName, 0.0, null, userName, userGroupId);
                groupReference.child(itemId).setValue(newItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Add the item to the local list and refresh adapter
                        shoppingList.add(newItem);
                        adapter.notifyDataSetChanged(); // Refreshes the RecyclerView
                        Toast.makeText(ShoppingListActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShoppingListActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item");

        final EditText input = new EditText(this);
        input.setText(item.getName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                item.setName(newName);
                databaseReference.child(item.getItemId()).setValue(item);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showMarkPurchasedDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Determine if marking or unmarking
        if (!item.isPurchased()) {
            builder.setTitle("Unmark as Purchased");
            builder.setMessage("Do you want to mark this item as unpurchased?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                item.setPurchased(false);
                item.setPrice(0.0);
                item.setPurchasedBy(null);
                item.setSelected(false);
                updateItemInFirebase(item);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        } else {
            builder.setTitle("Mark as Purchased");

            final EditText input = new EditText(this);
            input.setHint("Enter Price");
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String priceText = input.getText().toString().trim();
                if (!priceText.isEmpty()) {
                    double price = Double.parseDouble(priceText);
                    item.setPurchased(true);
                    item.setPrice(price);
                    item.setPurchasedBy(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    item.setSelected(true);
                    updateItemInFirebase(item);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        }
        builder.show();
    }

    private void markItemsAsPurchased() {
        for (Item item : shoppingList) {
            if (item.isSelected()) {  // Assuming you have an isSelected flag in the adapter
                item.setPurchased(true);
                item.setPurchasedBy(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                databaseReference.child(item.getItemId()).setValue(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteItem(Item item) {
        // Remove item from Firebase and update the list
        databaseReference.child(item.getItemId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                shoppingList.remove(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(ShoppingListActivity.this, "Item deleted at: ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ShoppingListActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void updateItemInFirebase(Item item) {
        databaseReference.child(item.getItemId()).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Item status updated", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to update item status", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Get user name from database
    public void getNameFromFireabse(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Fetch the user's name only once
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        userName = snapshot.getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ShoppingListActivity.this, "Failed to load user name.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

