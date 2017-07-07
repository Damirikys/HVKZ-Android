package org.hvkz.hvkz.db.firebase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GalleryDb extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "storage";
    public static final String TABLE_NAME = "gallery";

    public static final String ID = "_id";
    public static final String USERNAME = "username";
    public static final String URL = "url";
    public static final String PUBLISH_DATE = "pdate";
    public static final String DESCRIPTION = "descr";

    public GalleryDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + "("
                + ID + " integer,"
                + USERNAME + " text,"
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
