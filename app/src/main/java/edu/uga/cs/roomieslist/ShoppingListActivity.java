package edu.uga.cs.roomieslist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private DatabaseReference basketReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_list);

        // Assume `userGroupId` is retrieved from the user's profile upon login or signup
        userGroupId = getIntent().getStringExtra("GROUP_ID");

        // Reference to this group's shopping list in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);

        // Initialize the Firebase reference for the shopping basket
        basketReference = FirebaseDatabase.getInstance().getReference("ShoppingBasket").child(userGroupId);
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
        getNameFromFirebase();

        // Button for adding new items
        findViewById(R.id.addItemButton).setOnClickListener(v -> showAddItemDialog());

        findViewById(R.id.markPurchasedButton).setOnClickListener(v -> {
            markItemsAsPurchased(() -> { // Callback after marking items
                Intent intent = new Intent(ShoppingListActivity.this, ShoppingBasketActivity.class);
                intent.putExtra("GROUP_ID", userGroupId);
                startActivity(intent);
            });
        });



        // Handle Logout Button Click
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out the user
            Intent intent = new Intent(ShoppingListActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close current activity
        });
    }

    private void loadShoppingList() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    // Avoid duplicate items
                    if (item != null && !shoppingList.contains(item)){
                        shoppingList.add(item);
                    }
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

        // We need to create a layout in order to hold multiple input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input = new EditText(this);
        input.setHint("Item Name");
        layout.addView(input);

        final EditText amountInput = new EditText(this);
        amountInput.setHint("Amount");
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(amountInput);

        // Then we see the layout as the view for the dialog
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = input.getText().toString();
            String amountText = amountInput.getText().toString();

            if (!itemName.isEmpty()) {
                int amount = Integer.parseInt(amountText);
                DatabaseReference groupReference = FirebaseDatabase.getInstance().getReference("ShoppingList").child(userGroupId);
                String itemId = groupReference.push().getKey();
                Item newItem = new Item(itemId, itemName, 0.0, null, userName, userGroupId, amount);
                groupReference.child(itemId).setValue(newItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
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

        // Create a vertical layout to hold both input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        nameInput.setText(item.getName());
        layout.addView(nameInput);

        final EditText amountInput = new EditText(this);
        amountInput.setText(String.valueOf(item.getAmount()));
        amountInput.setHint("Item Amount");
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(amountInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString();
            String newAmountStr = amountInput.getText().toString();

            if (!newName.isEmpty() && !newAmountStr.isEmpty()) {
                int newAmount = Integer.parseInt(newAmountStr);
                item.setName(newName);
                item.setAmount(newAmount);
                FirebaseDatabase.getInstance()
                        .getReference("ShoppingList")
                        .child(userGroupId)
                        .child(item.getItemId())
                        .setValue(item)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
                            }
                        });
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

    private void markItemsAsPurchased(Runnable onComplete) {
        List<Item> itemsToUpdate = new ArrayList<>();

        // Collect items to move
        for (Item item : shoppingList) {
            if (item.isSelected()) {  // Assuming you have an isSelected flag in the adapter
                item.setPurchased(true);
                item.setPurchasedBy(FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Unknown User");
                itemsToUpdate.add(item); // Collect items to update

            }
        }

        // Process each item
        for (Item item : itemsToUpdate) {
            String itemId = item.getItemId();

            // Add the item to the ShoppingBasket in Firebase
            basketReference.child(itemId).setValue(item).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("ShoppingListActivity", "Item added to basket: " + item.getName());

                    // Remove the item from the ShoppingList in Firebase after adding to basket
                    databaseReference.child(itemId).removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            Log.d("ShoppingListActivity", "Item removed from shopping list: " + item.getName());
                            shoppingList.remove(item); // Update the local list
                            adapter.notifyDataSetChanged();

                        } else {
                            Log.e("ShoppingListActivity", "Failed to remove item from shopping list.");
                        }
                    });
                } else {
                    Log.e("ShoppingListActivity", "Failed to add item to basket.");
                }
            });
        }

        // Call callback if provided
        if (onComplete != null) {
            onComplete.run();
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
    public void getNameFromFirebase(){
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

    @Override
    protected void onResume() {
        super.onResume();
        loadShoppingList(); // Reload shopping list
    }

}

