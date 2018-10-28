package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    private InventoryDbHelper mDbHelpder;

    @Override
    public boolean onCreate() {
        mDbHelpder = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelpder.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Inseration is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
//        String supName = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
//        String supPhone = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

        if (name == null) {
            throw new IllegalArgumentException("A product name is required");
        }
        if (price != null && price < 1) {
            throw new IllegalArgumentException("A valid price is required");
        }
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("A valid quantity is required");
        }

        SQLiteDatabase db = mDbHelpder.getWritableDatabase();

        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelpder.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case INVENTORY:
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateItem(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product name required");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 1) {
                throw new IllegalArgumentException("A valid price is required");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("A valid quantity is required");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelpder.getWritableDatabase();
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
