package edu.uga.cs.roomieslist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * This is an adapter for the ShoppingListActivity.
 * Binds the data from the shopping list to the Views and enables users to edit, delete, and
 * update the items.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    // Variables
    private List<Item> shoppingList;
    private OnItemClickListener listener;

    /**
     * Constructor
     * @param shoppingList
     * @param listener
     */
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

    /**
     * Bind the data
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = shoppingList.get(position);
        holder.bind(item);
    }

    /**
     * Gets the total number of items at the shopping list
     * @return
     */
    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    /**
     * Manages each individual item View
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemNameTextView;
        private CheckBox selectedCheckBox;
        TextView purchaseStatusTextView;
        private TextView itemAmountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            selectedCheckBox = itemView.findViewById(R.id.selectedCheckBox);
            purchaseStatusTextView = itemView.findViewById(R.id.purchaseStatusTextView);
            itemAmountTextView = itemView.findViewById(R.id.itemAmountTextView);

            // Handles when checkbox is checked or unchecked
            selectedCheckBox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Item item = shoppingList.get(position);

                    // If the checkbox is checked then the item is purchased
                    item.setPurchased(selectedCheckBox.isChecked());

                    if (selectedCheckBox.isChecked()) {
                        listener.updateItemInFirebase(item);
                    } else {
                        // If item is unchecked then set the item as unpurchased
                        item.setPurchased(false);
                        item.setPrice(0.0);
                        item.setPurchasedBy(null);
                        item.setSelected(false);
                        listener.updateItemInFirebase(item);
                    }

                }
                notifyDataSetChanged();
            });

            // If item is clicked then it can be edited
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemEditClick(shoppingList.get(position));
                }
            });

            // If long-click, then item will be deleted
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemDeleteClick(shoppingList.get(position));
                }
                return true;
            });

        }

        /**
         * Binds the items of each View
         * @param item
         */
        public void bind(Item item) {
            itemNameTextView.setText(item.getName());
            selectedCheckBox.setChecked(item.isSelected());
            if (item.isPurchased()){
                purchaseStatusTextView.setText("Purchased");
            } else {
                purchaseStatusTextView.setText("Not Purchased");
            }
            String addedBy = item.getAddedBy() != null ? item.getAddedBy() : "Unknown User";
            itemAmountTextView.setText("Amount: " + item.getAmount());
        }
    }

    /**
     * Handles the items clicked
     */
    public interface OnItemClickListener {
        void onItemEditClick(Item item);
        void onItemDeleteClick(Item item);
        void updateItemInFirebase(Item item);
    }
}