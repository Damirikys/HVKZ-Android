package org.hvkz.hvkz.db.firebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.modules.home.Photo;

import java.util.ArrayList;
import java.util.List;

public class GalleryStorage
{
    private SQLiteDatabase database;
    private GalleryDb galleryDb;
    private static GalleryStorage instance;

    private GalleryStorage(Context c)
    {
        this.galleryDb = new GalleryDb(c);
        this.database = galleryDb.getWritableDatabase();
        try { galleryDb.onCreate(database); }
        catch (SQLiteException ignored){}
    }

    public void writeToGallery(Photo photo)
    {
        ContentValues contentValues = new ContentValues();
//        contentValues.put(GalleryDb.ID, photo.getId());
//        contentValues.put(GalleryDb.URL, photo.getUrl());
//        contentValues.put(GalleryDb.DESCRIPTION, photo.getDescription());
//        contentValues.put(GalleryDb.USERNAME, photo.getUsername());
//        contentValues.put(GalleryDb.PUBLISH_DATE, photo.getPublishDate());

        database.insert(GalleryDb.TABLE_NAME, null, contentValues);
    }

    public void deletePhoto(long id)
    {
        database.delete(GalleryDb.TABLE_NAME, GalleryDb.ID + "="+String.valueOf(id), null);
    }

    public void clearGallery()
    {
        database.delete(GalleryDb.TABLE_NAME, null, null);
    }

    public List<Photo> getUserPhoto(int limit, int offset)
    {
        List<Photo> photos = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + GalleryDb.TABLE_NAME +
                        " ORDER BY " + GalleryDb.ID + " DESC" +
                        " LIMIT "+ offset +","+ limit, null
        );

        if (cursor.moveToFirst())
        {
            int idIndex = cursor.getColumnIndex(GalleryDb.ID);
            int urlIndex = cursor.getColumnIndex(GalleryDb.URL);
            int descrIndex = cursor.getColumnIndex(GalleryDb.DESCRIPTION);
            int usrIndex = cursor.getColumnIndex(GalleryDb.USERNAME);
            int dateIndex = cursor.getColumnIndex(GalleryDb.PUBLISH_DATE);

            do {
                Photo currentPhoto = new Photo();
//                currentPhoto.setId(Long.parseLong(cursor.getString(idIndex)));
//                currentPhoto.setUrl(cursor.getString(urlIndex));
//                currentPhoto.setDescription(cursor.getString(descrIndex));
//                currentPhoto.setUsername(cursor.getString(usrIndex));
//                currentPhoto.setPublishDate(cursor.getString(dateIndex));

                photos.add(currentPhoto);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return photos;
    }

    public boolean isEmpty() {
        long cnt  = DatabaseUtils.queryNumEntries(database, GalleryDb.TABLE_NAME);
        return cnt == 0;
    }

    public static GalleryStorage getInstance() {
        if (instance == null) instance = new GalleryStorage(HVKZApp.getAppContext());
        return instance;
    }
}

