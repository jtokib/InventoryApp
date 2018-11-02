package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    Context context;

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView tvProductName = view.findViewById(R.id.product_name);
        TextView tvProductPrice = view.findViewById(R.id.product_price);
        TextView tvProductQuantity = view.findViewById(R.id.product_quantity);
        TextView tvSupplierName = view.findViewById(R.id.supplier_name);
        final TextView tvSupplierPhone = view.findViewById(R.id.supplier_phone);

//        Button callSupplierBtn = view.findViewById(R.id.call_supplier_list);
        Button sellItemBtn = view.findViewById(R.id.sale);
        Button productDetailBtn = view.findViewById(R.id.details);

        final int productId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        String productPrice = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE));
        final int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        String supplierName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_SUPPLIER));
        final String supplierPhone = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));

        tvProductName.setText("Name: " + productName);
        tvProductPrice.setText("Price: $" + productPrice);
        tvProductQuantity.setText("Quantity: " + productQuantity);

        if (TextUtils.isEmpty(supplierName)) {
            tvSupplierName.setVisibility(View.GONE);
        } else {
            tvSupplierName.setText("Supplier Name: " + supplierName);
        }

        if (TextUtils.isEmpty(supplierPhone)) {
            tvSupplierPhone.setVisibility(View.GONE);
        } else {
            tvSupplierPhone.setText("Supplier Phone: " + supplierPhone);
            tvSupplierPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PackageManager pm = context.getApplicationContext().getPackageManager();
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhone));
                    if (intent.resolveActivity(pm) != null) {
                        view.getContext().startActivity(intent);
                    }
                }
            });
        }

        productDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productId);

                Intent intent = new Intent(context, EditorActivity.class);
                intent.setData(currentItemUri);
                context.startActivity(intent);
            }
        });
        sellItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productId);
                ContentResolver contentResolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (productQuantity > 0) {
                    int updatedQuantity = productQuantity - 1;
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, updatedQuantity);
                    contentResolver.update(currentItemUri, values, null, null);
                    context.getContentResolver().notifyChange(currentItemUri, null);
                    Toast.makeText(context, "Item Sold", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Out of Stock Son", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
