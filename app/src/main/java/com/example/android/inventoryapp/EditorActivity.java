package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int IVENTORY_EDITOR_LOADER = 0;
    private Uri mCurrentItem;
    private EditText mProductNameEt;
    private EditText mProductPriceEt;
    private EditText mProductQuantityEt;
    private EditText mProductSupNameEt;
    private EditText mProductSupPhoneEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentItem = intent.getData();
        if (mCurrentItem == null) {
            setTitle("Add Item");
        } else {
            setTitle("Edit Item");
        }

        //Find views
        mProductNameEt = findViewById(R.id.edit_product_name);
        mProductPriceEt = findViewById(R.id.edit_product_price);
        mProductQuantityEt = findViewById(R.id.edit_product_quantity);
        mProductSupNameEt = findViewById(R.id.edit_supplier_name);
        mProductSupPhoneEt = findViewById(R.id.edit_supplier_phone);

        getSupportLoaderManager().initLoader(IVENTORY_EDITOR_LOADER, null, this);

    }

    private void insertItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEt.getText().toString().trim();
        String productPriceString = mProductPriceEt.getText().toString().trim();
        String productQuantityString = mProductQuantityEt.getText().toString().trim();
        String productSupName = mProductSupNameEt.getText().toString().trim();
        String productSupPhone = mProductSupPhoneEt.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, productSupName);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, productSupPhone);

        // Insert a new pet into the provider, returning the content URI for the new pet.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, "Item entered", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Entry failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        return new CursorLoader(this,
                mCurrentItem,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            //Find columns
            int productNameColIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int productSupNameColIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int productSupPhoneColIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            //Extract values
            String productName = cursor.getString(productNameColIndex);
            int productPrice = cursor.getInt(productPriceColIndex);
            int productQuantity = cursor.getInt(productQuantityColIndex);
            String productSupName = cursor.getString(productSupNameColIndex);
            String productSupPhone = cursor.getString(productSupPhoneColIndex);

            //Update views to values
            mProductNameEt.setText(productName);
            mProductPriceEt.setText(Integer.toString(productPrice));
            mProductQuantityEt.setText(Integer.toString(productQuantity));
            mProductSupNameEt.setText(productSupName);
            mProductSupPhoneEt.setText(productSupPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mProductNameEt.setText("");
        mProductPriceEt.setText("");
        mProductQuantityEt.setText("");
        mProductSupNameEt.setText("");
        mProductSupPhoneEt.setText("");
    }
}
