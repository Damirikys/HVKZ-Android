package org.hvkz.hvkz.modules.profile;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.firebase.storage.PhotoUploader;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

import static android.app.Activity.RESULT_OK;
import static org.hvkz.hvkz.modules.MainActivity.GALLERY_REQUEST;

public class ProfilePresenter extends BasePresenter implements FBStorageExecutor.ExecuteCallback<Photo>
{
    private static final String TAG = "ProfilePresenter";

    private GalleryStorage galleryStorage;

    public ProfilePresenter(BaseWindow activity) {
        super(activity);
        galleryStorage = GalleryStorage.getInstance();
    }

    public void uploadImage(Uri image) {
        PhotoUploader.with(getContext())
                .callback(this)
                .execute(image);
    }

    @Override
    public void onUploaded(Photo uploaded) {
        Log.d(TAG, "SUCCESS UPLOADED");
        getViewHandler(ProfileViewHandler.class)
                .getGalleryViewAdapter()
                .addPhoto(uploaded)
                .notifyItemInserted(0);

        galleryStorage.add(uploaded);
    }

    @Override
    public void onRemoved() {
        Log.d(TAG, "SUCCESS REMOVED");
    }

    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onProgress(long value, long max) {
        Log.d(TAG, "Progress: " + value + " из " + max);
    }

    @Override
    protected ViewHandler createViewHandler(BaseWindow activity) {
        return new ProfileViewHandler(activity);
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    uploadImage(dataIntent.getData());
                    break;
                default:
                    break;
            }
        }
    }
}
