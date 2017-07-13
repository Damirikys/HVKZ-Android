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
    public static final String STANZA_ID = "stanza";
    public static final String CHAT = "chat";
    public static final String JSON = "json";
    public static final String ISREAD = "readflag";

    public MessagesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + "("
                + ID + " integer primary key,"
                + STANZA_ID + " text,"
                + CHAT + " text,"
                + JSON + " text,"
                + ISREAD + " integer"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}