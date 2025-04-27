package com.example.dennisselfinger_new_project2;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

/**
 * InventoryAdapter connects database data to the RecyclerView
 * This adapter handles the display and interaction of inventory items in the grid layout
 *
 * @author Dennis Selfinger
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private Context context;
    private Cursor cursor;
    private DatabaseHelper databaseHelper;

    /**
     * Constructor initializes the adapter with context and cursor data
     * @param context Application context
     * @param cursor Database cursor containing inventory items
     */
    public InventoryAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        // Make sure cursor is not null and can move to position
        if (cursor == null || !cursor.moveToPosition(position)) {
            return;
        }

        // Get data from cursor
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));
        String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));

        // Set data to views
        holder.tvItemName.setText(itemName);
        holder.tvQuantity.setText(String.valueOf(quantity));
        holder.itemView.setTag(id); // Store ID for later use

        // Set up delete button click listener
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(id, itemName);
            }
        });

        // Set up item view click listener for editing
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditItemDialog(id, itemName, quantity, sku, location);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    /**
     * Updates the cursor with new data and notifies adapter
     * @param newCursor New cursor with updated data
     */
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for inventory items
     */
    class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity;
        Button btnDelete;

        InventoryViewHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    /**
     * Show dialog to confirm item deletion
     * @param id Item ID to delete
     * @param itemName Item name for display
     */
    private void deleteItem(int id, String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete " + itemName + "?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean success = databaseHelper.deleteInventoryItem(id);
                if (success) {
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    // Update the RecyclerView after delete
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).refreshInventoryData();
                    }
                } else {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     * Show dialog to edit an existing inventory item
     * Implements the Update functionality required in Project Three
     */
    private void showEditItemDialog(int id, String itemName, int quantity, String sku, String location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Inventory Item");

        // Inflate the dialog layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_item, null);

        // Get references to the EditText fields
        final EditText editTextItemName = view.findViewById(R.id.editTextItemName);
        final EditText editTextQuantity = view.findViewById(R.id.editTextQuantity);
        final EditText editTextSku = view.findViewById(R.id.editTextSku);
        final EditText editTextLocation = view.findViewById(R.id.editTextLocation);

        // Set current values
        editTextItemName.setText(itemName);
        editTextQuantity.setText(String.valueOf(quantity));
        editTextSku.setText(sku);
        editTextLocation.setText(location);

        // Set the custom view for the dialog
        builder.setView(view);

        // Add "Update" button to the dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get input values from EditText fields
                String updatedItemName = editTextItemName.getText().toString().trim();
                String quantityStr = editTextQuantity.getText().toString().trim();
                String updatedSku = editTextSku.getText().toString().trim();
                String updatedLocation = editTextLocation.getText().toString().trim();

                // Validate inputs
                if (updatedItemName.isEmpty() || quantityStr.isEmpty() || updatedSku.isEmpty()) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert quantity string to integer and update item in database
                int updatedQuantity = Integer.parseInt(quantityStr);
                boolean success = databaseHelper.updateInventoryItem(id, updatedItemName, updatedQuantity, updatedSku, updatedLocation);

                // Show success/failure message
                if (success) {
                    Toast.makeText(context, "Item updated successfully", Toast.LENGTH_SHORT).show();
                    // Update the RecyclerView after update
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).refreshInventoryData();
                    }
                } else {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add "Cancel" button to the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }
}