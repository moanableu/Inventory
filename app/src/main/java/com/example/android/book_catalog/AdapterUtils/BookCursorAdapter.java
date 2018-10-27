package com.example.android.book_catalog.AdapterUtils;

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

import com.example.android.book_catalog.R;

import java.text.NumberFormat;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;

public class BookCursorAdapter extends CursorAdapter {
    private Button saleButton;
    private TextView titleTextView, priceTextView, qtyTextView;
    private Uri entryUri;

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        saleButton = view.findViewById(R.id.sale_button);

        titleTextView = view.findViewById(R.id.book_title);
        priceTextView = view.findViewById(R.id.book_price);
        qtyTextView = view.findViewById(R.id.book_quantity);

        // read data via cursor interface?
        int titleColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
        double priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);

        // print data to screen
        String bookTitle = cursor.getString(titleColumnIndex);
        Double bookPrice = cursor.getDouble((int) priceColumnIndex);
        Integer bookQuantity = cursor.getInt(quantityColumnIndex);

        titleTextView.setText(bookTitle);

        if (bookPrice == 0) {
            priceTextView.setText(context.getString(R.string.label_Price));
        } else {
            priceTextView.setText(String.valueOf(bookPrice));
            NumberFormat priceNumber = NumberFormat.getInstance();
            priceNumber.setMinimumFractionDigits(2);
            priceNumber.setMaximumFractionDigits(2);
        }

        if (bookQuantity == 0) {
            qtyTextView.setText(context.getString(R.string.hint_quantity));
            saleButton.setVisibility(View.INVISIBLE);
            saleButton.setEnabled(false);
        } else {
            qtyTextView.setText(String.valueOf(bookQuantity));
            saleButton.setVisibility(View.VISIBLE);
            saleButton.setEnabled(true);
        }

        int currentId = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        entryUri = Uri.withAppendedPath(InventoryEntry.CONTENT_URI, Integer.toString(currentId));

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
