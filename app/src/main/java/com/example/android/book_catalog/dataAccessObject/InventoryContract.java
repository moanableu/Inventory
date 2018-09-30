package com.example.android.book_catalog.dataAccessObject;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_BIOGRAPHY;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_FICTION;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_ILLUSTRATED;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.GENRE_NON_FICTION;
import static com.example.android.book_catalog.dataAccessObject.InventoryContract.InventoryEntry.NOT_DEFINED;

public final class InventoryContract {

    // empty constructor to prevent contract instantiation
    private InventoryContract(){}

    /**
     * create all the constants required for the ContentResolver - goal is to provide access to data
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.book_catalog";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "books";

    public static final class InventoryEntry implements BaseColumns{ //BaseColumns interface already implements column ID, no ID column is required here

        // enable access to the whole table
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // enable access to a particular row(s)
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        // access data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        // table name
        public final static String TABLE_NAME = "books";

        // column headers
        public static final String _ID = BaseColumns._ID;
        public final static String COLUMN_BOOK_TITLE = "book_title";
        public final static String COLUMN_AUTHOR = "author";
        public final static String COLUMN_GENRE = "genre";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY = "quantity";
        public final static String COLUMN_PUBLISHER_NAME = "publisher_name";
        public final static String COLUMN_PUBLISHER_PHONE = "publisher_phone";

        // genre available options
        public static final int NOT_DEFINED = 0;
        public static final int GENRE_FICTION = 1;
        public static final int GENRE_BIOGRAPHY = 2;
        public static final int GENRE_ILLUSTRATED = 3;
        public static final int GENRE_NON_FICTION = 4;
    }

    public static boolean isValidGenre (int genre){
        if (genre == NOT_DEFINED || genre == GENRE_FICTION || genre == GENRE_BIOGRAPHY ||
                genre == GENRE_ILLUSTRATED || genre == GENRE_NON_FICTION){
            return true;
        }
        return false;
    }
}
