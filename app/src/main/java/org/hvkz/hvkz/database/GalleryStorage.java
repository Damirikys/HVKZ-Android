package org.hvkz.hvkz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GalleryStorage
{
    @Inject
    User user;

    private SQLiteDatabase database;
    private GalleryDbHelper galleryDb;
    private static GalleryStorage instance;

    private GalleryStorage(Context c) {
        HVKZApp.component().inject(this);
        this.galleryDb = new GalleryDbHelper(c);
        this.database = galleryDb.getWritableDatabase();
        try { galleryDb.onCreate(database); }
        catch (SQLiteException ignored){}
    }

    public void add(Photo photo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GalleryDbHelper.UID, user.getUserId());
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
                        " WHERE " + GalleryDbHelper.UID + "=" + user.getUserId() +
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

    public static GalleryStorage getInstance() {
        if (instance == null) instance = new GalleryStorage(HVKZApp.getAppContext());
        return instance;
    }
}

