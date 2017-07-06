package com.reserver;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBProvider {
    private DBUtility dbUtility = null;
    private SQLiteDatabase reservationDatabase = null;

    private boolean isOpened = false;

    public DBProvider(Context context) {
        dbUtility = new DBUtility(context);
    }

    // Open the database.
    public void open() {
        if (!isOpened) {
            reservationDatabase = dbUtility.getWritableDatabase();
            if (reservationDatabase == null) {
                return;
            }

            isOpened = true;
        }
    }

    // Add a row to the database.
    public void addEntry(String guestPhone, String guestEmail, int guestsCount, String dateTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBUtility.COLUMN_GUEST_PHONE, guestPhone);
        contentValues.put(DBUtility.COLUMN_GUEST_EMAIL, guestEmail);
        contentValues.put(DBUtility.COLUMN_GUESTS_COUNT, guestsCount);
        contentValues.put(DBUtility.COLUMN_DATE_TIME, dateTime);
        reservationDatabase.insert(DBUtility.TABLE_NAME, null, contentValues);
    }

    // Close the database.
    public void close() {
        if (isOpened) {
            dbUtility.close();
            reservationDatabase = null;
            isOpened = false;
        }
    }
}
