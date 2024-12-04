package edu.uga.cs.roomieslist;

import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PurchasedItemsAdapter extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder> {

    private final List<PurchasedRecord> purchasedRecords;
    private final OnItemClickListener listener;
    private final String groupId; // Pass groupId directly to the adapter

    // Interface for click actions
    public interface OnItemClickListener {
        void onUpdatePriceClick(PurchasedRecord record, double newPrice);
    }

    // Constructor
    public PurchasedItemsAdapter(List<PurchasedRecord> purchasedRecords, String groupId, OnItemClickListener listener) {
        this.purchasedRecords = purchasedRecords;
        this.groupId = groupId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchased_record, parent, false); // Ensure correct layout reference
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchasedRecord record = purchasedRecords.get(position);

        // Bind data to views
        String purchaserName = record.getPurchasedBy() != null ? record.getPurchasedBy() : "Unknown User";
        holder.purchaseRoommateTextView.setText("Purchased by: " + purchaserName);
        holder.purchaseTotalPriceTextView.setText(String.format(Locale.US, "Total Price: $%.2f", record.getTotalPrice()));

        // Convert timestamp to readable date
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(record.getTimestamp());
        holder.purchaseTimestampTextView.setText("Purchased on: " + date);

        // Build a comma-separated string of items
        List<Item> items = record.getItems();
        if (items != null && !items.isEmpty()) {
            StringBuilder itemsStringBuilder = new StringBuilder("Items: ");
            for (Item item : items) {
                if (item != null && item.getName() != null) { // Check for null item and name
                    itemsStringBuilder.append(item.getName()).append(", ");
                }
            }
            String itemsString = itemsStringBuilder.toString().trim();
            if (itemsString.endsWith(",")) {
                itemsString = itemsString.substring(0, itemsString.length() - 1); // Remove trailing comma
            }
            holder.purchaseItemsTextView.setText(itemsString);
        } else {
            holder.purchaseItemsTextView.setText("Items: None");
        }

        // Handle edit group action
        holder.editGroupButton.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EditPurchasedRecordActivity.class);
            intent.putExtra("GROUP_ID", groupId); // Pass groupId directly
            intent.putExtra("PURCHASE_ID", record.getId());
            context.startActivity(intent);
        });

        // Handle updating the price when the item view is clicked
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                showUpdatePriceDialog(v.getContext(), record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return purchasedRecords.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView purchaseRoommateTextView;
        private final TextView purchaseTotalPriceTextView;
        private final TextView purchaseTimestampTextView;
        private final TextView purchaseItemsTextView;
        private final Button editGroupButton; // Added button for editing group items

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            purchaseRoommateTextView = itemView.findViewById(R.id.purchaseRoommateTextView);
            purchaseTotalPriceTextView = itemView.findViewById(R.id.purchaseTotalPriceTextView);
            purchaseTimestampTextView = itemView.findViewById(R.id.purchaseTimestampTextView);
            purchaseItemsTextView = itemView.findViewById(R.id.purchaseItemsTextView);
            editGroupButton = itemView.findViewById(R.id.editGroupButton); // Initialize edit button
        }
    }

    // Dialog to update the price of a purchase
    private void showUpdatePriceDialog(Context context, PurchasedRecord record) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Update Purchase Price");

        // Input for new price
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String inputText = input.getText().toString();
            if (!inputText.isEmpty()) {
                double newPrice = Double.parseDouble(inputText);
                if (listener != null) {
                    listener.onUpdatePriceClick(record, newPrice);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
