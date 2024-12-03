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

import java.util.List;

public class PurchasedItemsAdapter extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder> {

    private List<PurchasedRecord> purchasedRecords;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemRemoveClick(PurchasedRecord record, Item item);
        void onUpdatePriceClick(PurchasedRecord record, double newPrice);
    }

    public PurchasedItemsAdapter(List<PurchasedRecord> purchasedRecords, OnItemClickListener listener) {
        this.purchasedRecords = purchasedRecords;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchased_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchasedRecord record = purchasedRecords.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return purchasedRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView purchaseInfoTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            purchaseInfoTextView = itemView.findViewById(R.id.purchaseInfoTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PurchasedRecord record = purchasedRecords.get(position);
                    showUpdatePriceDialog(v.getContext(), record);
                }
            });
        }

        public void bind(PurchasedRecord record) {
            purchaseInfoTextView.setText(String.format("%s\n%s\n$%.2f", record.getPurchasedBy(),
                    record.getTimestamp(), record.getTotalPrice()));
        }

        private void showUpdatePriceDialog(Context context, PurchasedRecord record) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Purchase Price");

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
}