package org.hvkz.hvkz.modules.home;

import android.net.Uri;

import org.hvkz.hvkz.db.firebase.PhotosDb;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

public class ExecutePhotoController implements FBStorageExecutor.ExecuteCallback<Uri>
{
    private String description;
    private long timestamp;

    private FBStorageExecutor.ExecuteCallback<Photo> callback;

    public ExecutePhotoController(FBStorageExecutor.ExecuteCallback<Photo> callback) {
        this.timestamp = System.currentTimeMillis();
        this.callback = callback;
    }

    public ExecutePhotoController changeTask(String desc) {
        this.description = desc;
        this.timestamp = System.currentTimeMillis();
        return this;
    }

    @Override
    public void onUploaded(Uri uploadedUri) {
        Photo photo = new Photo(uploadedUri.toString())
                .setDate(timestamp)
                .setDescription(description);

        PhotosDb.uploadPhoto(photo, value -> {
            if (value) {
                callback.onUploaded(photo);
            } else {
                callback.onFailure(new Exception("Not upload."));
            }
        });
    }

    @Override
    public void onRemoved() {
        callback.onRemoved();
    }

    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
        callback.onFailure(e);
    }

    @Override
    public void onProgress(long value, long max) {
        callback.onProgress(value, max);
    }
}
