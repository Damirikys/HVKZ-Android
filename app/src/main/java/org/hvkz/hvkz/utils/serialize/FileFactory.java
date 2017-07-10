package org.hvkz.hvkz.utils.serialize;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.hvkz.hvkz.HVKZApp;

import java.io.File;

public class FileFactory
{
    public static File getFileFromUri(Uri contentURI) {
        String result;
        Cursor cursor = HVKZApp.getAppContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return new File(result);
    }
}
