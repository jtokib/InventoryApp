package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvProductName = view.findViewById(R.id.product_name);
        TextView tvProductPrice = view.findViewById(R.id.product_price);
        TextView tvProductQuantity = view.findViewById(R.id.product_quantity);
        TextView tvSupplierName = view.findViewById(R.id.supplier_name);
        TextView tvSupplierPhone = view.findViewById(R.id.supplier_phone);

        String productName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        String productPrice = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE));
        String productQuantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        String supplierName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_SUPPLIER));
        String supplierPhone = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE));

        tvProductName.setText("Product Name: " + productName);
        tvProductPrice.setText("Price: $" + productPrice);
        tvProductQuantity.setText("Quantity: " + productQuantity);

        if (supplierName == null) {
            tvSupplierName.setVisibility(View.GONE);
        } else {
            tvSupplierName.setText("Supplier Name: " + supplierName);
        }

        if (supplierPhone == null) {
            tvSupplierPhone.setVisibility(View.GONE);
        } else {
            tvSupplierPhone.setText("Supplier Phone: " + supplierPhone);
        }
    }
}
