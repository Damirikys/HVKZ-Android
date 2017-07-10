package org.hvkz.hvkz.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GalleryDbHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "storage";
    public static final String TABLE_NAME = "gallery";

    public static final String ID = "_id";
    public static final String UID = "uid";
    public static final String URL = "url";
    public static final String PUBLISH_DATE = "date";
    public static final String DESCRIPTION = "desc";

    public GalleryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + UID + " integer,"
                + URL + " text,"
                + DESCRIPTION + " text,"
                + PUBLISH_DATE + " text"
                + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}
