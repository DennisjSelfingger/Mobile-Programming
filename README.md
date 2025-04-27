# Mobile-Programming
Android Studio inventory app
Inventory Management App

A simple mobile app for Android built with Android Studio that allows users to manage inventory items, track quantities, and receive SMS alerts for low-stock items.

This my.... Dennis Selfinger's..... first full Android Studio project — just the beginning stages of building a complete inventory management system.

Features

User Authentication

Login with existing accounts

Create a new account (username/password)

Inventory Management

Add new inventory items with quantity, SKU, and optional location

Edit existing inventory items

Delete inventory items

View inventory items in a scrollable list/grid

SMS Notifications

Sends an SMS alert if any inventory item's quantity reaches zero (requires permission)

Simple and Clean UI

Organized screens for login, inventory list, and item management

Floating action button to quickly add new items

Material Design components

Tech Stack

Java (Primary language)

SQLite (Local database)

Android Studio (IDE)

Material Design Components

RecyclerView (for displaying inventory list)

Project Structure

File

Description

DatabaseHelper.java

Handles all database operations like user creation, login, inventory CRUD actions

LoginActivity.java

Allows users to log in or create an account

MainActivity.java

Displays inventory items, allows adding, editing, deleting, and sending SMS alerts

InventoryAdapter.java

Adapter for connecting inventory data to the RecyclerView

XML Layout Files

Layout File

Purpose

activity_login.xml

Login screen layout

activity_main.xml

Main inventory management screen

dialog_add_item.xml

Dialog for adding/editing items

item_inventory.xml

Design for each inventory item row

activity_inventory_alerts.xml

Optional notification settings screen

row_item.xml

Alternative inventory row layout with checkboxes and images

activity_data_display.xml

Experimental or future detailed inventory display

new_activity_main.xml

Experimental alternate main layout

######  How to Run It ####

Open Android Studio.

Clone or copy the project files into a new project.

Connect an Android emulator or device.

Click Run to build and launch the app!

Allow SMS permission if you want to enable SMS notifications.

Notes

The app works even without SMS permission — notifications are optional.

Some experimental layouts (like new_activity_main.xml) were started but not finalized.

Future features may include barcode scanning, images for items, cloud database support, etc.

Screenshots



Author

Dennis SelfingerFirst project using Android StudioCS 360 - Mobile App Development Coursework

"This is just the beginning!" 🚀
