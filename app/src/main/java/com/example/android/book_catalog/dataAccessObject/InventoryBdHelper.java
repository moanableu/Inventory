package com.example.android.book_catalog.dataAccessObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.book_catalog.dataAccessObject.InventoryContract.*;

public class InventoryBdHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = InventoryBdHelper.class.getSimpleName();

    // database information:
    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor that will instantiate the DB in the app context
     * @param context
     */
    public InventoryBdHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_AUTHOR + " TEXT, "
                + InventoryEntry.COLUMN_GENRE + " TEXT, "
                + InventoryEntry.COLUMN_PRICE + " DOUBLE NOT NULL, "
                + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_PUBLISHER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PUBLISHER_PHONE + " INTEGER NOT NULL );";

        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // to be added
    }
}
