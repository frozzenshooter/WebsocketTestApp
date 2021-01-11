package com.garlic.websockettest.messages;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDatabaseHelper extends SQLiteOpenHelper {

    // Metadata
    public static final String DB_NAME = "messages";
    public static final String TABLE_NAME = "message_table";
    public static final int DB_VERSION = 1;

    // Columns
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String MESSAGE = "MESSAGE";

    MessageDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+TABLE_NAME+"("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MESSAGE + " NUMERIC, "
                + TIMESTAMP + " NUMERIC );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
