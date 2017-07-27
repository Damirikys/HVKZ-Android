package org.hvkz.hvkz.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Photo;

import java.util.ArrayList;
import java.util.List;


public class GalleryStorage
{
    private HVKZApp app;
    private SQLiteDatabase database;
    private GalleryDbHelper galleryDb;

    public GalleryStorage(HVKZApp app) {
        this.app = app;
        this.galleryDb = new GalleryDbHelper(app);
        this.database = galleryDb.getWritableDatabase();
        try { galleryDb.onCreate(database); }
        catch (SQLiteException ignored){}
    }

    public void add(Photo photo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GalleryDbHelper.UID, app.getCurrentUser().getUserId());
        contentValues.put(GalleryDbHelper.URL, photo.getUrl());
        contentValues.put(GalleryDbHelper.DESCRIPTION, photo.getDescription());
        contentValues.put(GalleryDbHelper.PUBLISH_DATE, String.valueOf(photo.getDate()));

        database.insert(GalleryDbHelper.TABLE_NAME, null, contentValues);
    }

    public void addAll(List<Photo> photos) {
        database.beginTransaction();
        for (Photo photo : photos)
            add(photo);
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void remove(Photo photo) {
        database.delete(GalleryDbHelper.TABLE_NAME, GalleryDbHelper.PUBLISH_DATE + "=" + String.valueOf(photo.getDate()), null);
    }

    public void clear() {
        database.delete(GalleryDbHelper.TABLE_NAME, null, null);
    }

    public List<Photo> getPhotos(int limit, int offset) {
        List<Photo> photos = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + GalleryDbHelper.TABLE_NAME +
                        " WHERE " + GalleryDbHelper.UID + "=" + app.getCurrentUser().getUserId() +
                        " ORDER BY " + GalleryDbHelper.ID + " DESC" +
                        " LIMIT "+ offset +","+ limit, null
        );

        if (cursor.moveToFirst()) {
            int urlIndex = cursor.getColumnIndex(GalleryDbHelper.URL);
            int descrIndex = cursor.getColumnIndex(GalleryDbHelper.DESCRIPTION);
            int dateIndex = cursor.getColumnIndex(GalleryDbHelper.PUBLISH_DATE);

            do {
                Photo currentPhoto = new Photo()
                        .setUrl(cursor.getString(urlIndex))
                        .setDescription(cursor.getString(descrIndex))
                        .setDate(Long.valueOf(cursor.getString(dateIndex)));

                photos.add(currentPhoto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return photos;
    }

    public boolean isEmpty() {
        long cnt  = DatabaseUtils.queryNumEntries(database, GalleryDbHelper.TABLE_NAME);
        return cnt == 0;
    }
}

