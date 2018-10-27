package com.example.android.book_catalog.dataAccessObject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.CONTENT_AUTHORITY;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.PATH_BOOKS;

public class BookProvider extends ContentProvider {
    private InventoryDBHelper mDbHelper;

    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    // set index range starting at 1000
    public static final int BOOKS = 1000;

    // set index for first item
    public static final int BOOK_ID = 1001;

    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //initialize the class
    static {
        // enable a relation to ALL items in the Db
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS, BOOKS);
        // enable a relation to a single item in the Db
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                c = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(("Unknown URI " + uri));
        }
        // set notification to update cursor if uri data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    /**
     * validate insertion values
     *
     * @param uri
     * @param values
     * @return
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        String title = values.getAsString(InventoryEntry.COLUMN_BOOK_TITLE);
        if (title == null || title.isEmpty()) {
            return null;
        }

        String author = values.getAsString(InventoryEntry.COLUMN_AUTHOR);
        if (author == null || author.isEmpty()) {
            return null;
        }

        Double price = values.getAsDouble(InventoryEntry.COLUMN_PRICE);
        if (price == 0 || price < 0) {
            return null;
        }

        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 0) {
            return null;
        }

        String publisher = values.getAsString(InventoryEntry.COLUMN_PUBLISHER_NAME);
        if (publisher == null || publisher.isEmpty()) {
            return null;
        }

        Long publisherPhone = values.getAsLong(InventoryEntry.COLUMN_PUBLISHER_PHONE);
        if (publisherPhone == null || publisherPhone < 0) {
            return null;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed insertion at row " + uri);
            return null;
        }

        // notify listeners of data change
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryEntry.COLUMN_BOOK_TITLE)) {
            String title = values.getAsString(InventoryEntry.COLUMN_BOOK_TITLE);
            if (title == null || title.isEmpty()) {
                return 0;
            }
        }

        String author = values.getAsString(InventoryEntry.COLUMN_AUTHOR);
        if (author == null || author.isEmpty()) {
            return 0;
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRICE)) {
            Double price = values.getAsDouble(InventoryEntry.COLUMN_PRICE);
            if (price == 0 || price < 0) {
                return 0;
            }
        }

        // is this the only one being updated?
        if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0) {
                return 0;
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PUBLISHER_NAME)) {
            String publisher = values.getAsString(InventoryEntry.COLUMN_PUBLISHER_NAME);
            if (publisher == null || publisher.isEmpty()) {
                return 0;
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PUBLISHER_PHONE)) {
            String publisherPhone = values.getAsString(InventoryEntry.COLUMN_PUBLISHER_PHONE);
            //
            if (publisherPhone == null || publisherPhone.isEmpty()) {
                return 0;
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // once all validations have passed get a hold of the writable db and update
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case BOOK_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
}
