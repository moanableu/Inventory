package com.example.android.book_catalog;

import android.content.ContentValues;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

    private Spinner mGenreSpinner;

    /**
     * Book genre {@link InventoryEntry#GENRE_NOT_DEFINED}
     * {@link InventoryEntry#GENRE_FICTION}, {@link InventoryEntry#GENRE_BIOGRAPHY},
     * {@link InventoryEntry#GENRE_ILLUSTRATED}, {@link InventoryEntry#GENRE_NON_FICTION}
     */
    private int mGenre = InventoryEntry.GENRE_NOT_DEFINED;

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
        mGenreSpinner = findViewById(R.id.spinner_genre);

        mEditTitle.setOnTouchListener(mTouchListener);
        mEditAuthor.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditPublisher.setOnTouchListener(mTouchListener);
        mEditPhone.setOnTouchListener(mTouchListener);
        mGenreSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        mEditPhone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditPhone.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupSpinner() {

        ArrayAdapter genreSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_genre,
                android.R.layout.simple_spinner_item);

        genreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenreSpinner.setAdapter(genreSpinnerAdapter);

        mGenreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView <?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equalsIgnoreCase(getString(R.string.fiction))) {
                        mGenre = InventoryEntry.GENRE_FICTION;
                    } else if (selection.equalsIgnoreCase(getString(R.string.biography))) {
                        mGenre = InventoryEntry.GENRE_BIOGRAPHY;
                    } else if (selection.equalsIgnoreCase(getString(R.string.illustrated))) {
                        mGenre = InventoryEntry.GENRE_ILLUSTRATED;
                    } else if (selection.equalsIgnoreCase(getString(R.string.non_fiction))) {
                        mGenre = InventoryEntry.GENRE_NON_FICTION;
                    } else {
                        mGenre = InventoryEntry.GENRE_NOT_DEFINED;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
                mGenre = InventoryEntry.GENRE_NOT_DEFINED;
            }
        });
    }

    private void saveBook() {
        String titleString = mEditTitle.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String priceString = mEditPrice.getText().toString().trim();
        String quantity = mEditQuantity.getText().toString().trim();
        String publisherString = mEditPublisher.getText().toString().trim();
        String phoneString = mEditPhone.getText().toString().trim();

        if (mCurrentBookUri == null && TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(authorString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantity) && TextUtils.isEmpty(publisherString) &&
                TextUtils.isEmpty(phoneString) && mGenre == InventoryEntry.GENRE_NOT_DEFINED) {
            // on empty entry return to main
            return;
        }

        // test OR empty data NOT WORKING
        /*if (mCurrentBookUri == null && TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(authorString) || TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantity) || TextUtils.isEmpty(publisherString) ||
                TextUtils.isEmpty(phoneString) || mGenre == InventoryEntry.GENRE_NOT_DEFINED) {
            // show which entries are mandatory
            Toast t = Toast.makeText(ManageInventoryActivity.this,
                    R.string.manage_act_invalid_entry,
                    Toast.LENGTH_LONG);
            t.setGravity(Gravity.BOTTOM, 0, 260);
            t.show();
            return;
        }*/

        ContentValues cv = new ContentValues();
        cv.put(InventoryEntry.COLUMN_BOOK_TITLE, titleString);
        cv.put(InventoryEntry.COLUMN_AUTHOR, authorString);
        cv.put(InventoryEntry.COLUMN_GENRE, mGenre);
        cv.put(InventoryEntry.COLUMN_QUANTITY, quantity);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_NAME, publisherString);
        cv.put(InventoryEntry.COLUMN_PUBLISHER_PHONE, phoneString);

        // by default set price to 0 if not provided
        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        cv.put(InventoryEntry.COLUMN_PRICE, price);

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
                Toast.makeText(this, R.string.manage_act_update_entry_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.manage_act_update_ok, Toast.LENGTH_SHORT).show();
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
                saveBook();
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
        b.setPositiveButton(R.string.discard, discardButtonClickListener);
        b.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
        b.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEntry();
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

    private void deleteEntry() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.manage_act_delete_entry_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.manage_act_delete_entry_ok, Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }


    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
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
                    mGenreSpinner.setSelection(1);
                    break;
                case InventoryEntry.GENRE_BIOGRAPHY:
                    mGenreSpinner.setSelection(2);
                    break;
                case InventoryEntry.GENRE_ILLUSTRATED:
                    mGenreSpinner.setSelection(3);
                    break;
                case InventoryEntry.GENRE_NON_FICTION:
                    mGenreSpinner.setSelection(4);
                    break;
                default:
                    mGenreSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader <Cursor> loader) {
        mEditTitle.setText("");
        mEditAuthor.setText("");
        mGenreSpinner.setSelection(0);
        mEditPrice.setText("");
        mEditQuantity.setText("");
        mEditPublisher.setText("");
        mEditPhone.setText("");
    }
}