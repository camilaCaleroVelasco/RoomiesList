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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            selectedCheckBox = itemView.findViewById(R.id.selectedCheckBox);

            // Mark as purchased when checkbox is clicked
            selectedCheckBox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemPurchasedClick(shoppingList.get(position));
                }
            });

            // Edit item when clicked
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemEditClick(shoppingList.get(position));
                }
            });

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
        }
    }

    // Interface for item click actions
    public interface OnItemClickListener {
        void onItemPurchasedClick(Item item);
        void onItemEditClick(Item item);
        void onItemDeleteClick(Item item);
    }
}