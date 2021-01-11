package com.garlic.websockettest.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garlic.websockettest.ApplicationContext;

public class MessageHandler {

    private static final String TAG = MessageHandler.class.getSimpleName();
    private SQLiteOpenHelper messageDatabaseHelper;

    public MessageHandler(@NonNull Context context){
        messageDatabaseHelper = new MessageDatabaseHelper(context);
    }

    public void addMessage(@NonNull Double message){
        SQLiteDatabase db = messageDatabaseHelper.getWritableDatabase();

        ContentValues newMessage = new ContentValues();
        Long timestamp = System.currentTimeMillis()/1000;

        newMessage.put(MessageDatabaseHelper.TIMESTAMP, timestamp);
        newMessage.put(MessageDatabaseHelper.MESSAGE, message);
        db.insert(MessageDatabaseHelper.TABLE_NAME, null, newMessage);
        db.close();
    }

    public int resetMessages(){
        SQLiteDatabase db = messageDatabaseHelper.getWritableDatabase();

        int amountOfRecords = 0;
        try {
            amountOfRecords = db.delete(MessageDatabaseHelper.TABLE_NAME, "1", null);
        }catch (Exception e){
            Log.w(TAG, e);
        }
        db.close();
        return amountOfRecords;
    }

}
