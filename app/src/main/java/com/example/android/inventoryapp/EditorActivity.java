package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int IVENTORY_EDITOR_LOADER = 0;
    private Uri mCurrentItemUri;

    private boolean mItemHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

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
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(R.string.editor_activity_add_item_title);
            invalidateOptionsMenu();
            findViewById(R.id.call_supplier_editor).setVisibility(View.GONE);

        } else {
            setTitle(R.string.editor_activity_edit_item_title);
            getSupportLoaderManager().initLoader(IVENTORY_EDITOR_LOADER, null, this);
        }

        //Find views
        mProductNameEt = findViewById(R.id.edit_product_name);
        mProductPriceEt = findViewById(R.id.edit_product_price);
        mProductQuantityEt = findViewById(R.id.edit_product_quantity);
        mProductSupNameEt = findViewById(R.id.edit_supplier_name);
        mProductSupPhoneEt = findViewById(R.id.edit_supplier_phone);

        //Set onTouch listeners for any changes
        mProductNameEt.setOnTouchListener(mTouchListener);
        mProductPriceEt.setOnTouchListener(mTouchListener);
        mProductQuantityEt.setOnTouchListener(mTouchListener);
        mProductSupNameEt.setOnTouchListener(mTouchListener);
        mProductSupPhoneEt.setOnTouchListener(mTouchListener);

        //Buttons
        Button callSupplier = findViewById(R.id.call_supplier_editor);
        ImageButton increaseQuantity = findViewById(R.id.increase_quantity);
        ImageButton decreaseQuantity = findViewById(R.id.decrease_quantity);

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productQuantity = mProductQuantityEt.getText().toString().trim();
                if (!TextUtils.isEmpty(productQuantity)) {
                    Integer quantity = Integer.parseInt(productQuantity);
                    quantity++;
                    mProductQuantityEt.setText(String.valueOf(quantity));
                }
            }
        });

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productQuantity = mProductQuantityEt.getText().toString().trim();
                if (!TextUtils.isEmpty(productQuantity)) {
                    Integer quantity = Integer.parseInt(productQuantity);
                    if (quantity > 0) {
                        quantity--;
                    } else {
                        quantity = 0;
                        Toast.makeText(getApplicationContext(), R.string.editor_activity_invalid_quantity, Toast.LENGTH_SHORT).show();
                    }
                    mProductQuantityEt.setText(String.valueOf(quantity));
                }
            }
        });

        callSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mProductSupPhoneEt.getText().toString()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    private void saveItem() {
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
        if (TextUtils.isEmpty(productNameString)) {
            Toast.makeText(this, R.string.editor_activity_name_required, Toast.LENGTH_SHORT).show();
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        }
        if (TextUtils.isEmpty(productPriceString)) {
            Toast.makeText(this, R.string.editor_activity_price_required, Toast.LENGTH_SHORT).show();
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceString);
        }
        if (TextUtils.isEmpty(productQuantityString)) {
            Toast.makeText(this, R.string.editor_activity_quantity_required, Toast.LENGTH_SHORT).show();
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantityString);
        }
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER, productSupName);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, productSupPhone);

        if (mCurrentItemUri == null && TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(productPriceString) && TextUtils.isEmpty(productQuantityString) && TextUtils.isEmpty(productSupName) && TextUtils.isEmpty(productSupPhone)) {
            return;
        }

        if (mCurrentItemUri == null) {
            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, R.string.editor_activity_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_activity_item_entered, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, InventoryActivity.class);
                startActivity(intent);
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.editor_activity_updated_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_activity_item_updated, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, InventoryActivity.class);
                startActivity(intent);
            }
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_activity_show_unsaved_prompt);
        builder.setPositiveButton(R.string.editor_activity_show_unsaved_positive, discardClickListener);
        builder.setNegativeButton(R.string.editor_activity_show_unsaved_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_confirmation_single_item);
        builder.setPositiveButton(R.string.delete_confirmation_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.delete_confirmation_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_activity_delete_fail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_activity_deleted, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
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
                saveItem();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        return new CursorLoader(this,
                mCurrentItemUri,
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

        if (cursor.moveToFirst() && mCurrentItemUri != null) {
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
