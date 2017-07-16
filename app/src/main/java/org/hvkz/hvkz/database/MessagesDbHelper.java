package org.hvkz.hvkz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessagesDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "storage";
    public static final String TABLE_NAME = "messages";

    public static final String ID = "_id";
    public static final String UID = "uid";
    public static final String CHAT = "chat";
    public static final String JSON = "json";
    public static final String STANZA_ID = "stanza";
    public static final String READ_MARK = "read";

    public MessagesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + UID + " integer,"
                + CHAT + " text,"
                + JSON + " text,"
                + STANZA_ID + " text,"
                + READ_MARK + " integer"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}