package com.example.android.book_catalog;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.book_catalog.AdapterUtils.BookCursorAdapter;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks <Cursor> {
    public static final int LOADER = 0;

    //private InventoryDBHelper mDbHelper;

    BookCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ManageInventoryActivity.class);
                startActivity(i);
            }
        });

        ListView bookListView = (ListView) findViewById(R.id.list_view_book);
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, ManageInventoryActivity.class);

                Uri currentBookUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                i.setData(currentBookUri);
                startActivity(i);
            }
        });

        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        //Button saleButton = new BookCursorAdapter.SaleButton();

        getSupportLoaderManager().initLoader(LOADER, null, this);
    }


    private void insertEntry() {
        // dummy data insert for test purpose
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, "El Ruisenor");
        values.put(InventoryEntry.COLUMN_AUTHOR, "Leonardo Costa");
        values.put(InventoryEntry.COLUMN_GENRE, "Fiction");
        values.put(InventoryEntry.COLUMN_PUBLISHER_PHONE, "59357721845");
        values.put(InventoryEntry.COLUMN_PRICE, 9.15);
        values.put(InventoryEntry.COLUMN_QUANTITY, 2);
        values.put(InventoryEntry.COLUMN_PUBLISHER_NAME, "Ariel");

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        Log.v("Inventory activity", "new row added");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insert_dummy_data:
                insertEntry();
                return true;
            case R.id.delete_entries:
                deleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("Main Activity ", rowsDeleted + " rows deleted from DB");
    }

    @NonNull
    @Override
    public android.support.v4.content.Loader <Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BOOK_TITLE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PRICE
        };
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader <Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader <Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void deleteConfirmationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(R.string.delete_book_warning);
        b.setPositiveButton(R.string.delete_all_entries, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllEntries();
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = b.create();
        alertDialog.show();
    }
}
