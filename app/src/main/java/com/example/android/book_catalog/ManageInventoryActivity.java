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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.book_catalog.AdapterUtils.ReusableMethods;
import com.santalu.widget.MaskEditText;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_AUTHOR;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_BOOK_TITLE;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_GENRE;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_PRICE;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_PUBLISHER_NAME;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_PUBLISHER_PHONE;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.COLUMN_QUANTITY;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.CONTENT_URI;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_BIOGRAPHY;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_FICTION;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_ILLUSTRATED;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_NON_FICTION;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_NOT_DEFINED;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry._ID;

/**
 * Mask EditText resource: https://github.com/santalu/mask-edittext
 * Inspired by Coding in Flow tutorial: https://www.youtube.com/watch?v=4bbF4I_ZaG4
 */

public class ManageInventoryActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks <Cursor> {

    private static final String TAG = ManageInventoryActivity.class.getSimpleName();

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;

    private EditText mEditTitle, mEditAuthor, mEditQuantity, mEditPublisher;

    private MaskEditText mEditPrice, mEditPhone;

    private String titleData, authorData, phoneData, priceData, quantityData, publisherData;

    private Button increase, decrease;

    private int quantity = 0;

    private Spinner mGenreSpinner;

    private View mFabButton;

    /**
     * Book genre {@link InventoryEntry#GENRE_NOT_DEFINED}
     * {@link InventoryEntry#GENRE_FICTION}, {@link InventoryEntry#GENRE_BIOGRAPHY},
     * {@link InventoryEntry#GENRE_ILLUSTRATED}, {@link InventoryEntry#GENRE_NON_FICTION}
     */
    private int mGenre = GENRE_NOT_DEFINED;

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

    /**
     * Listener decreased inventory
     */
    private View.OnClickListener decreaseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String qty = mEditQuantity.getText().toString();
            if (qty.isEmpty() || quantity == 0) {
                Toast.makeText(ManageInventoryActivity.this, R.string.negative_inventory_warning, Toast.LENGTH_SHORT).show();
            } else {
                quantity -= 1;
                qty = String.valueOf(quantity);
                mEditQuantity.setText(qty);
            }
        }
    };

    /**
     * Listener increased inventory
     */
    private View.OnClickListener increaseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String qty = mEditQuantity.getText().toString();
            if (qty.isEmpty() || quantity >= 0) {
                quantity += 1;
                qty = String.valueOf(quantity);
                mEditQuantity.setText(qty);
            }
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

        mEditTitle = findViewById(R.id.book_title);
        mEditAuthor = findViewById(R.id.book_author);
        mEditPrice = findViewById(R.id.book_price);
        mEditQuantity = findViewById(R.id.quantity);
        mEditPublisher = findViewById(R.id.publisher);
        mEditPhone = findViewById(R.id.publisher_phone);
        mGenreSpinner = findViewById(R.id.spinner_genre);
        increase = findViewById(R.id.increase_button);
        decrease = findViewById(R.id.decrease_button);
        mFabButton = findViewById(R.id.call_action);

        mEditTitle.setOnTouchListener(mTouchListener);
        mEditAuthor.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditPublisher.setOnTouchListener(mTouchListener);
        mEditPhone.setOnTouchListener(mTouchListener);
        mGenreSpinner.setOnTouchListener(mTouchListener);
        increase.setOnClickListener(increaseListener);
        decrease.setOnClickListener(decreaseListener);

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

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                String phone = mEditPhone.getRawText();

                i.setData(Uri.parse("tel:" + phone));
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                }
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
                        mGenre = GENRE_FICTION;
                    } else if (selection.equalsIgnoreCase(getString(R.string.biography))) {
                        mGenre = GENRE_BIOGRAPHY;
                    } else if (selection.equalsIgnoreCase(getString(R.string.illustrated))) {
                        mGenre = GENRE_ILLUSTRATED;
                    } else if (selection.equalsIgnoreCase(getString(R.string.non_fiction))) {
                        mGenre = GENRE_NON_FICTION;
                    } else {
                        mGenre = GENRE_NOT_DEFINED;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
                mGenre = GENRE_NOT_DEFINED;
            }
        });
    }

    private void saveBook() {
        String titleString = mEditTitle.getText().toString().trim();
        String authorString = mEditAuthor.getText().toString().trim();
        String priceString = mEditPrice.getRawText();
        String quantityString = mEditQuantity.getText().toString().trim();
        String publisherString = mEditPublisher.getText().toString().trim();
        String phoneString = mEditPhone.getRawText();

        if (mCurrentBookUri == null && TextUtils.isEmpty(titleString) &&
                TextUtils.isEmpty(authorString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(publisherString) &&
                TextUtils.isEmpty(phoneString) && mGenre == GENRE_NOT_DEFINED) {
            // on empty entry return to main
            return;
        }

        // validate all fields
        if (TextUtils.isEmpty(titleString)) {
            mandatoryFieldsToast();
        } else if (TextUtils.isEmpty(authorString)) {
            mandatoryFieldsToast();
        } else if (TextUtils.isEmpty(priceString)) {
            mandatoryFieldsToast();
        } else if (TextUtils.isEmpty(quantityString)) {
            mandatoryFieldsToast();
        } else if (TextUtils.isEmpty(publisherString)) {
            mandatoryFieldsToast();
        } else if (TextUtils.isEmpty(phoneString)) {
            mandatoryFieldsToast();
        } else {

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_BOOK_TITLE, titleString);
            cv.put(COLUMN_AUTHOR, authorString);
            cv.put(COLUMN_GENRE, mGenre);
            cv.put(COLUMN_PUBLISHER_NAME, publisherString);

            // casted fields
            double price = Double.parseDouble(priceString);
            cv.put(COLUMN_PRICE, price);

            int quantity = Integer.parseInt(quantityString);
            cv.put(COLUMN_QUANTITY, quantity);

            long phone = Long.parseLong(phoneString);
            cv.put(COLUMN_PUBLISHER_PHONE, phone);

            if (mCurrentBookUri == null) {
                Uri newUri = getContentResolver().insert(CONTENT_URI, cv);

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
            finish();
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
                return true;
            case R.id.menu_delete:
                ReusableMethods.deleteConfirmationDialog(this, mCurrentBookUri);
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

    public void mandatoryFieldsToast() {
        Toast toast = Toast.makeText(this, R.string.mandatory_fields_warning, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
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

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                _ID,
                COLUMN_BOOK_TITLE,
                COLUMN_AUTHOR,
                COLUMN_GENRE,
                COLUMN_PRICE,
                COLUMN_QUANTITY,
                COLUMN_PUBLISHER_NAME,
                COLUMN_PUBLISHER_PHONE};
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
            int titleIndex = data.getColumnIndex(COLUMN_BOOK_TITLE);
            int authorIndex = data.getColumnIndex(COLUMN_AUTHOR);
            int genreIndex = data.getColumnIndex(COLUMN_GENRE);
            int priceIndex = data.getColumnIndex(COLUMN_PRICE);
            int quantityIndex = data.getColumnIndex(COLUMN_QUANTITY);
            int publisherIndex = data.getColumnIndex(COLUMN_PUBLISHER_NAME);
            int publisherPhoneIndex = data.getColumnIndex(COLUMN_PUBLISHER_PHONE);

            String title = data.getString(titleIndex);
            String author = data.getString(authorIndex);
            int genre = data.getInt(genreIndex);
            double price = data.getDouble(priceIndex);
            int quantity = data.getInt(quantityIndex);
            String publisher = data.getString(publisherIndex);
            long publisherPhone = data.getInt(publisherPhoneIndex);

            mEditTitle.setText(title);
            mEditAuthor.setText(author);
            mEditPrice.setText(Double.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));
            mEditPublisher.setText(publisher);
            mEditPhone.setText(Long.toString(publisherPhone));

            switch (genre) {
                case GENRE_FICTION:
                    mGenreSpinner.setSelection(1);
                    break;
                case GENRE_BIOGRAPHY:
                    mGenreSpinner.setSelection(2);
                    break;
                case GENRE_ILLUSTRATED:
                    mGenreSpinner.setSelection(3);
                    break;
                case GENRE_NON_FICTION:
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