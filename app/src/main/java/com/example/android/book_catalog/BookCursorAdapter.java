package com.example.android.book_catalog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;

public class BookCursorAdapter extends CursorAdapter {
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        Button saleButton = view.findViewById(R.id.sale_button);

        TextView titleTextView = view.findViewById(R.id.title);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView qtyTextView = view.findViewById(R.id.quantity);

        int titleColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
        double priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        String bookTitle = cursor.getString(titleColumnIndex);
        Double bookPrice = cursor.getDouble((int) priceColumnIndex);
        Integer bookQuantity = cursor.getInt(quantityColumnIndex);

        titleTextView.setText(bookTitle);

        if (bookPrice == 0) {
            priceTextView.setText(context.getString(R.string.hint_Price));
        } else {
            priceTextView.setText(String.valueOf(bookPrice));
        }

        if (bookQuantity == 0) {
            qtyTextView.setText(context.getString(R.string.quantity));
            saleButton.setVisibility(View.INVISIBLE);
            saleButton.setEnabled(false);
        } else {
            qtyTextView.setText(String.valueOf(bookQuantity));
            saleButton.setVisibility(View.VISIBLE);
            saleButton.setEnabled(true);
        }

        int currentId = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        final Uri entryUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, Integer.toString(currentId));

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.valueOf(qtyTextView.getText().toString());
                if (quantity > 0) {
                    quantity -= 1;
                } else {
                    Toast.makeText(context, "No books in stock", Toast.LENGTH_SHORT).show();
                }

                ContentValues cv = new ContentValues();
                cv.put(InventoryEntry.COLUMN_QUANTITY, quantity);
                context.getContentResolver().update(entryUri, cv, null, null);
            }
        });
    }


}
