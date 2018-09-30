package com.example.android.book_catalog;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.book_catalog.dataAccessObject.InventoryDBHelper;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.*;

public class MainActivity extends AppCompatActivity {

    private InventoryDBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ManageInventoryActivity.class);
                startActivity(i);
            }
        });

        mDbHelper = new InventoryDBHelper(this);
        displayDb();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDb();
    }

    private void displayDb() {

        // define columns to be queried
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BOOK_TITLE,
                InventoryEntry.COLUMN_AUTHOR,
                InventoryEntry.COLUMN_GENRE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PUBLISHER_NAME,
                InventoryEntry.COLUMN_PUBLISHER_PHONE};

        Cursor c = getContentResolver().query(
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        TextView displayView = (TextView) findViewById(R.id.display_book);

        try {
            displayView.setText("The books table contains " + c.getCount() + " books.\n\n");

            displayView.append(InventoryEntry._ID + " || " +
                    InventoryEntry.COLUMN_BOOK_TITLE + " || " +
                    InventoryEntry.COLUMN_AUTHOR + " || " +
                    InventoryEntry.COLUMN_GENRE + " || " +
                    InventoryEntry.COLUMN_PRICE + " || " +
                    InventoryEntry.COLUMN_QUANTITY + " || " +
                    InventoryEntry.COLUMN_PUBLISHER_NAME + " || " +
                    InventoryEntry.COLUMN_PUBLISHER_PHONE + "\n"
            );

            //associate each column with corresponding index position
            int idColumnIndex = c.getColumnIndex(InventoryEntry._ID);
            int titleColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
            int authorColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_AUTHOR);
            int genreColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_GENRE);
            int priceColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int qtyColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int publisherColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_PUBLISHER_NAME);
            int pPhoneColumnIndex = c.getColumnIndex(InventoryEntry.COLUMN_PUBLISHER_PHONE);

            while(c.moveToNext()){
                int currentID = c.getInt(idColumnIndex);
                String currentTitle = c.getString(titleColumnIndex);
                String currentAuthor = c.getString(authorColumnIndex);
                int currentGenre = c.getInt(genreColumnIndex);
                double currentPrice = c.getDouble(priceColumnIndex);
                int currentQty = c.getInt(qtyColumnIndex);
                String currentPublisher = c.getString(publisherColumnIndex);
                int currentPPhone = c.getInt(pPhoneColumnIndex);

                displayView.append(("\n\n" +
                        currentID + " | " +
                        currentTitle + " | " +
                        currentAuthor + " | " +
                        currentGenre + " | " +
                        currentPrice + " | " +
                        currentQty + " | " +
                        currentPublisher + " | " +
                        currentPPhone)
                );
            }
        } finally {
            c.close();
        }
    }

    private void insertEntry() {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, "El Ruisenor");
        values.put(InventoryEntry.COLUMN_AUTHOR, "Leonardo Costa");
        values.put(InventoryEntry.COLUMN_GENRE, "fiction");
        values.put(InventoryEntry.COLUMN_PUBLISHER_PHONE, "+593.577218");
        values.put(InventoryEntry.COLUMN_PRICE, 12.12);
        values.put(InventoryEntry.COLUMN_QUANTITY, 2);
        values.put(InventoryEntry.COLUMN_PUBLISHER_NAME, "Ariel");

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI,values);
        Log.v("Inventory activity", "new row added" );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();

        switch (item.getItemId()) {
            //noinspection SimplifiableIfStatement
            case R.id.insert_dummy_data:
                insertEntry();
                displayDb();
                return true;
            case R.id.delete_entries:
                // method not implemented for the moment
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
