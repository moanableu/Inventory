package com.example.android.book_catalog;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = view.findViewById(R.id.title_s);
        TextView summaryTextView = view.findViewById(R.id.summary);

        int titleColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
        int genreColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_GENRE);

        String bookTitle = cursor.getString(titleColumnIndex);
        String bookGenre = cursor.getString(genreColumnIndex);

        if (TextUtils.isEmpty(bookGenre)) {
            bookGenre = context.getString(R.string.unspecified_genre);
        }

        titleTextView.setText(bookTitle);
        summaryTextView.setText(bookGenre);
    }
}
