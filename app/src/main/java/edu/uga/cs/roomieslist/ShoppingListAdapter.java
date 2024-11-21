package edu.uga.cs.roomieslist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<Item> shoppingList;
    private OnItemClickListener listener;

    // Constructor
    public ShoppingListAdapter(List<Item> shoppingList, OnItemClickListener listener){
        this.shoppingList = shoppingList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = shoppingList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    // ViewHolder class for managing each item view
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemNameTextView;
        private CheckBox selectedCheckBox;
        TextView addedByTextView;
        TextView purchaseStatusTextView;
        private TextView itemAmountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            selectedCheckBox = itemView.findViewById(R.id.selectedCheckBox);
            addedByTextView = itemView.findViewById(R.id.addedByTextView);
            purchaseStatusTextView = itemView.findViewById(R.id.purchaseStatusTextView);
            itemAmountTextView = itemView.findViewById(R.id.itemAmountTextView);

            // Handle when checkbox is checked or unchecked
            selectedCheckBox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Item item = shoppingList.get(position);

                    // Toggle purchased status
                    item.setPurchased(selectedCheckBox.isChecked());

                    if (selectedCheckBox.isChecked()) {
                        // When checking the item, prompt to mark it as purchased
                        listener.updateItemInFirebase(item);
                    } else {
                        // When unchecking, immediately mark it as unpurchased
                        item.setPurchased(false);
                        item.setPrice(0.0);
                        item.setPurchasedBy(null);
                        listener.updateItemInFirebase(item); // Directly update Firebase without dialog
                    }

                    notifyItemChanged(position); // Refresh item view
                }
            });

            // Edit item when clicked
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemEditClick(shoppingList.get(position));
                }
            });

            // Delete item when long-clicked
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemDeleteClick(shoppingList.get(position));
                }
                return true;
            });

        }

        public void bind(Item item) {
            itemNameTextView.setText(item.getName()); // Bind the item name
            selectedCheckBox.setChecked(item.isSelected()); // Bind selection state
            if (item.isPurchased()){
                purchaseStatusTextView.setText("Purchased");
            } else {
                purchaseStatusTextView.setText("Not Purchased");
            }
            String addedBy = item.getAddedBy() != null ? item.getAddedBy() : "Unknown User";
            addedByTextView.setText("Added by: " + addedBy);
            itemAmountTextView.setText("Amount: " + item.getAmount());
        }
    }

    // Interface for item click actions
    public interface OnItemClickListener {
        void onItemEditClick(Item item);
        void onItemDeleteClick(Item item);
        void updateItemInFirebase(Item item);
    }
}