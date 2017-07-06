package com.reserver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBUtility extends SQLiteOpenHelper {

    // Database description.
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "reservation.db";
    public static final String TABLE_NAME = "reservation_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GUEST_PHONE = "guest_phone";
    public static final String COLUMN_GUEST_EMAIL = "guest_email";
    public static final String COLUMN_GUESTS_COUNT = "guests_count";
    public static final String COLUMN_DATE_TIME = "date_time";

    // Database create command.
    public static final String DB_CREATE = "create table "
            + TABLE_NAME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_GUEST_PHONE + " text not null, "
            + COLUMN_GUEST_EMAIL + " text not null, "
            + COLUMN_GUESTS_COUNT + " integer, "
            + COLUMN_DATE_TIME + " text not null "
            + ");";

    public DBUtility(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
