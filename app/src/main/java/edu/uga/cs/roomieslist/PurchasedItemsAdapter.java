package edu.uga.cs.roomieslist;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    // Interface for click actions
    public interface OnItemClickListener {
        void onItemRemoveClick(PurchasedRecord record, Item item);
        void onUpdatePriceClick(PurchasedRecord record, double newPrice);
    }

    // Constructor
    public PurchasedItemsAdapter(List<PurchasedRecord> purchasedRecords, OnItemClickListener listener) {
        this.purchasedRecords = purchasedRecords;
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
        holder.purchaseRoommateTextView.setText("Purchased by: " + record.getPurchasedBy());
        holder.purchaseTotalPriceTextView.setText(String.format(Locale.US, "Total Price: $%.2f", record.getTotalPrice()));

        // Convert timestamp to readable date
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(record.getTimestamp());
        holder.purchaseTimestampTextView.setText("Purchased on: " + date);

        // Build a comma-separated string of items
        StringBuilder itemsStringBuilder = new StringBuilder("Items: ");
        for (Item item : record.getItems()) {
            itemsStringBuilder.append(item.getName()).append(", ");
        }
        String itemsString = itemsStringBuilder.toString().trim();
        if (itemsString.endsWith(",")) {
            itemsString = itemsString.substring(0, itemsString.length() - 1); // Remove trailing comma
        }
        holder.purchaseItemsTextView.setText(itemsString);

        // Handle item clicks for updating price
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
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView purchaseRoommateTextView;
        private final TextView purchaseTotalPriceTextView;
        private final TextView purchaseTimestampTextView;
        private final TextView purchaseItemsTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            purchaseRoommateTextView = itemView.findViewById(R.id.purchaseRoommateTextView);
            purchaseTotalPriceTextView = itemView.findViewById(R.id.purchaseTotalPriceTextView);
            purchaseTimestampTextView = itemView.findViewById(R.id.purchaseTimestampTextView);
            purchaseItemsTextView = itemView.findViewById(R.id.purchaseItemsTextView);
        }
    }

    // Dialog to update the price of a purchase
    private void showUpdatePriceDialog(Context context, PurchasedRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Purchase Price");

        // Input for new price
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            double newPrice = Double.parseDouble(input.getText().toString());
            listener.onUpdatePriceClick(record, newPrice);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
