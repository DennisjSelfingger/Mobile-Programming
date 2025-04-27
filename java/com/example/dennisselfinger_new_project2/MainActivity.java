//Dennis Selfinger
// I did my best to comment so it would be easier to follow my thinking!!! lol hopefully anyway

package com.example.dennisselfinger_new_project2;

// Import necessary Android libraries
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Button;
import android.content.Intent;


/**
 * MainActivity class handles the main inventory grid display and SMS functionality
 * This class will allow users to view all inventory items, add new items,
 * and receive SMS notifications for zero-quantity items.
 *
 * For CS 360 Project Three - Mobile Inventory App
 * @author Dennis Selfinger
 */
public class MainActivity extends AppCompatActivity {
    // Define a constant for our SMS permission request code
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;

    // Declare variables for UI components and database
    private RecyclerView recyclerViewInventory;  // RecyclerView will display our inventory grid
    private InventoryAdapter inventoryAdapter;   // Custom adapter for our inventory items
    private DatabaseHelper databaseHelper;       // Helper class for database operations
    private FloatingActionButton fabAddItem;     // Floating action button to add new items
    private boolean smsPermissionGranted = false; // Track if SMS permission is granted

    /**
     * onCreate is called when the activity is first created
     * Here is where i am  setting  up the UI components and initialize the database
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Back & Home buttons
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonHome = findViewById(R.id.buttonHome);

        buttonBack.setOnClickListener(v -> finish());

        buttonHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            startActivity(homeIntent);
        });

        // Initialize our database helper to interact with SQLite
        databaseHelper = new DatabaseHelper(this);

        // Connect Java variables to the UI elements
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        fabAddItem = findViewById(R.id.fabAddItem);

        // Set up RecyclerView (vertical list + dividers)
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerViewInventory.getContext(),
                LinearLayoutManager.VERTICAL
        );
        recyclerViewInventory.addItemDecoration(divider);

        loadInventoryData();      // Load data into RecyclerView
        requestSmsPermission();   // Ask SMS permission
        checkForZeroQuantityItems(); // Send any zero‑quantity alerts

        // Floating action button to add new items
        fabAddItem.setOnClickListener(v -> showAddItemDialog());
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize our database helper to interact with SQLite
//        databaseHelper = new DatabaseHelper(this);
//
//        // Connect our Java variables to the UI elements defined in XML
//        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
//        fabAddItem = findViewById(R.id.fabAddItem);
//
//        // Set up RecyclerView with a LinearLayoutManager (displays items in a vertical list)
//        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
//
//        // Add divider lines between items in the RecyclerView
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
//                recyclerViewInventory.getContext(),
//                LinearLayoutManager.VERTICAL);
//        recyclerViewInventory.addItemDecoration(dividerItemDecoration);
//
//        loadInventoryData(); // Load data from database into RecyclerView
//
//        // Request permission to send SMS notifications
//        // This is required for Project Three requirements
//        requestSmsPermission();
//
//        // Set up click listener for the floating action button
//        // When clicked, it will show dialog to add new inventory items
//        fabAddItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAddItemDialog();
//            }
//        });
//
//        // Check if any inventory items are at zero quantity
//        // If found, send SMS notifications (if permission granted)
//        checkForZeroQuantityItems();
//    }

    /**
     * Request permission to send SMS messages
     * Android requires explicit permission from the user for SMS functionality
     */
    private void requestSmsPermission() {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // If not granted, request it from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Permission was previously granted
            smsPermissionGranted = true;
        }
    }

    /**
     * Handle the result of the permission request
     * This method is called after the user responds to the permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted permission
                smsPermissionGranted = true;
                // Now that we have permission, check for zero-quantity items
                checkForZeroQuantityItems();
            }
            // Even if permission denied, the app should continue to function
            // This fulfills the requirement that app works without SMS permission
        }
    }

    /**
     * Send SMS notification for zero-quantity items
     * For this project, we're using a test number (replace in production)
     * @param itemName The name of the item that reached zero quantity
     */
    private void sendSmsNotification(String itemName) {
        if (smsPermissionGranted) {
            try {
                // Use Android's SmsManager to send text message
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(
                        "5555555555", // In a real app, this would be a configurable phone number
                        null,
                        "Inventory Alert: " + itemName + " has reached zero quantity!",
                        null,
                        null);
                Toast.makeText(this, "SMS notification sent for " + itemName, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "SMS failed to send", Toast.LENGTH_SHORT).show();
                e.printStackTrace(); // Log error for debugging
            }
        }
    }

    /**
     * Check database for items with zero quantity and send notifications
     * This is a key feature required for Project Three
     */
    private void checkForZeroQuantityItems() {
        // Query database for items with quantity = 0
        Cursor cursor = databaseHelper.getZeroQuantityItems();
        if (cursor.moveToFirst()) {
            do {
                // For each zero-quantity item, get the name and send notification
                int itemNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME);
                if (itemNameIndex != -1) {
                    String itemName = cursor.getString(itemNameIndex);
                    sendSmsNotification(itemName);
                }
            } while (cursor.moveToNext());
        }
        cursor.close(); // Always close cursor to prevent memory leaks
    }

    /**
     * Load inventory data from database and display in RecyclerView
     * This creates the grid display tha the project asked for
     */
    private void loadInventoryData() {
        // Get all inventory items from database
        Cursor cursor = databaseHelper.getAllInventoryItems();
        // Create adapter with the cursor data
        inventoryAdapter = new InventoryAdapter(this, cursor);
        // Set adapter on RecyclerView to display the data
        recyclerViewInventory.setAdapter(inventoryAdapter);
    }

    /**
     * Refresh inventory data after changes (add, edit, delete)
     * This keeps our UI in sync with the database
     */
    public void refreshInventoryData() {
        // Get fresh data from database
        Cursor newCursor = databaseHelper.getAllInventoryItems();
        // Update adapter with new data
        inventoryAdapter.swapCursor(newCursor);
        // Check if any items now have zero quantity
        checkForZeroQuantityItems();
    }

    /**
     * Show dialog for adding a new inventory item
     * This creates the interface for the "Create" functionality required in Project Three
     */
    private void showAddItemDialog() {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Inventory Item");

        // Inflate the dialog layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        // Get references to the EditText fields
        final EditText editTextItemName = view.findViewById(R.id.editTextItemName);
        final EditText editTextQuantity = view.findViewById(R.id.editTextQuantity);
        final EditText editTextSku = view.findViewById(R.id.editTextSku);
        final EditText editTextLocation = view.findViewById(R.id.editTextLocation);

        // Set the custom view for the dialog
        builder.setView(view);

        // Add "Add" button to the dialog
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get input values from EditText fields
                String itemName = editTextItemName.getText().toString().trim();
                String quantityStr = editTextQuantity.getText().toString().trim();
                String sku = editTextSku.getText().toString().trim();
                String location = editTextLocation.getText().toString().trim();

                // Validate inputs (make sure required fields aren't empty)
                if (itemName.isEmpty() || quantityStr.isEmpty() || sku.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert quantity string to integer and add item to database
                int quantity = Integer.parseInt(quantityStr);
                boolean success = databaseHelper.addInventoryItem(itemName, quantity, sku, location);

                // Show success/failure message
                if (success) {
                    Toast.makeText(MainActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    refreshInventoryData(); // Refresh to display the new item
                } else {
                    Toast.makeText(MainActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add "Cancel" button to the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Just close the dialog, no action
            }
        });

        // Show the dialog
        builder.show();
    }
}