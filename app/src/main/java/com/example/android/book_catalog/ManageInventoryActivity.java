package com.example.android.book_catalog;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.*;

public class ManageInventoryActivity extends AppCompatActivity {

    private EditText mEditTitle, mEditAuthor, mEditPrice, mEditQuantity, mEditPublisher, mEditPhone;

    private Spinner mEditGenre;

    private int mGenre = InventoryEntry.NOT_DEFINED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        mEditTitle = (EditText) findViewById(R.id.title);
        mEditAuthor = (EditText) findViewById(R.id.author);
        mEditPrice = (EditText) findViewById(R.id.price);
        mEditQuantity = (EditText) findViewById(R.id.quantity);
        mEditPublisher = (EditText) findViewById(R.id.publisher);
        mEditPhone = (EditText) findViewById(R.id.phone);

        mEditGenre = (Spinner) findViewById(R.id.spinner_genre);

        setupSpinner();
    }

    private void setupSpinner() {

        ArrayAdapter genreSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_genre,
                android.R.layout.simple_spinner_item);

        genreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mEditGenre.setAdapter(genreSpinnerAdapter);

        mEditGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(R.string.fiction)) {
                        mGenre = InventoryEntry.GENRE_FICTION;
                    } else if (selection.equals(R.string.biography)) {
                        mGenre = InventoryEntry.GENRE_BIOGRAPHY;
                    } else if (selection.equals(R.string.illustrated)) {
                        mGenre = InventoryEntry.GENRE_ILLUSTRATED;
                    } else if (selection.equals(R.string.fiction)) {
                        mGenre = InventoryEntry.GENRE_NON_FICTION;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
                mGenre = InventoryEntry.NOT_DEFINED;
            }
        });
    }

    private void insertBook(){
        String titleString = mEditTitle.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String publisherString = mEditPublisher.getText().toString().trim();
        String phoneString = mEditPhone.getText().toString().trim();

        // insert price and qty here

        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_BOOK_TITLE, titleString);
        cv.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_NAME, publisherString);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_PHONE, phoneString);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, cv);

        if (newUri == null){
            Toast.makeText(this, "Error saving book", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                insertBook();
                finish();
                return true;
            case R.id.delete:
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}