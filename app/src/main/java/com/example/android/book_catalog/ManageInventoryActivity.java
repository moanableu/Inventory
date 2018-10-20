package com.example.android.book_catalog;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;

public class ManageInventoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks <Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private EditText mEditTitle, mEditAuthor, mEditPrice, mEditQuantity, mEditPublisher, mEditPhone;

    private Spinner mEditGenre;

    private int mGenre = InventoryEntry.NOT_DEFINED;

    private boolean mBookUpdated = false;

    /**
     * monitor any change in state on a given view
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBookUpdated = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        // determine intent used to launch activity: new or edit.
        Intent i = getIntent();
        mCurrentBookUri = i.getData();

        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.manage_act_new_book));
            // hide Delete option
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.manage_act_edit_book));
            getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mEditTitle = findViewById(R.id.title);
        mEditAuthor = findViewById(R.id.author);
        mEditPrice = findViewById(R.id.price);
        mEditQuantity = findViewById(R.id.quantity);
        mEditPublisher = findViewById(R.id.publisher);
        mEditPhone = findViewById(R.id.phone);
        mEditGenre = findViewById(R.id.spinner_genre);

        mEditTitle.setOnTouchListener(mTouchListener);
        mEditAuthor.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditPublisher.setOnTouchListener(mTouchListener);
        mEditPhone.setOnTouchListener(mTouchListener);
        mEditGenre.setOnTouchListener(mTouchListener);

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

    private void insertBook() {
        String titleString = mEditTitle.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String price = mEditPrice.getText().toString().trim();
        String quantity = mEditQuantity.getText().toString().trim();
        String publisherString = mEditPublisher.getText().toString().trim();
        String phoneString = mEditPhone.getText().toString().trim();

        if (mCurrentBookUri == null && TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(authorString) && TextUtils.isEmpty(price) &&
                TextUtils.isEmpty(quantity) && TextUtils.isEmpty(publisherString) &&
                TextUtils.isEmpty(phoneString) && mGenre == InventoryEntry.NOT_DEFINED) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_BOOK_TITLE, titleString);
        cv.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        cv.put(InventoryEntry.COLUMN_PRICE, price);
        cv.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_NAME, publisherString);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_PHONE, phoneString);

        if (mCurrentBookUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, cv);

            if (newUri == null) {
                Toast.makeText(this, "Error saving book", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentBookUri,
                    cv, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.manage_act_update_ok,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.menu_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                insertBook();
                finish();
                return true;
            case R.id.menu_delete:
                deleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookUpdated) {
                    NavUtils.navigateUpFromSameTask(ManageInventoryActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(ManageInventoryActivity.this);
                            }
                        };
                unsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mBookUpdated) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        unsavedChangesDialog(discardButtonClickListener);
    }

    private void unsavedChangesDialog
            (DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(R.string.unsaved_changes_warning);
        b.setPositiveButton("Discard", discardButtonClickListener);
        b.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
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

    private void deleteConfirmationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(R.string.delete_book_warning);
        b.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEntry();
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    private void deleteEntry() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.manage_act_delete_entry_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.manage_act_delete_entry_ok, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //TBC
    @NonNull
    @Override
    public Loader <Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BOOK_TITLE,
                InventoryEntry.COLUMN_AUTHOR,
                InventoryEntry.COLUMN_GENRE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_PUBLISHER_NAME,
                InventoryEntry.COLUMN_PUBLISHER_PHONE};
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader <Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int titleIndex = data.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
            int authorIndex = data.getColumnIndex(InventoryEntry.COLUMN_AUTHOR);
            int genreIndex = data.getColumnIndex(InventoryEntry.COLUMN_GENRE);
            int priceIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityIndex = data.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int publisherIndex = data.getColumnIndex(InventoryEntry.COLUMN_PUBLISHER_NAME);
            int publisherPhoneIndex = data.getColumnIndex(InventoryEntry.COLUMN_PUBLISHER_PHONE);

            String title = data.getString(titleIndex);
            String author = data.getString(authorIndex);
            int genre = data.getInt(genreIndex);
            double price = data.getDouble(priceIndex);
            int quantity = data.getInt(quantityIndex);
            String publisher = data.getString(publisherIndex);
            int publisherPhone = data.getInt(publisherPhoneIndex);

            mEditTitle.setText(title);
            mEditAuthor.setText(author);
            mEditPrice.setText(Double.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));
            mEditPublisher.setText(publisher);
            mEditPhone.setText(Integer.toString(publisherPhone));

            switch (genre) {
                case InventoryEntry.GENRE_FICTION:
                    mEditGenre.setSelection(1);
                    break;
                case InventoryEntry.GENRE_BIOGRAPHY:
                    mEditGenre.setSelection(2);
                    break;
                case InventoryEntry.GENRE_ILLUSTRATED:
                    mEditGenre.setSelection(3);
                    break;
                case InventoryEntry.GENRE_NON_FICTION:
                    mEditGenre.setSelection(4);
                    break;
                default:
                    mEditGenre.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader <Cursor> loader) {
        mEditTitle.setText("");
        mEditAuthor.setText("");
        mEditGenre.setSelection(0);
        mEditPrice.setText("");
        mEditQuantity.setText("");
        mEditPublisher.setText("");
        mEditPhone.setText("");
    }
}