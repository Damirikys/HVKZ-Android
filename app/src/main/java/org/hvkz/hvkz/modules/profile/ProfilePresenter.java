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
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

import static android.app.Activity.RESULT_OK;
import static org.hvkz.hvkz.modules.MainActivity.GALLERY_REQUEST;

public class ProfilePresenter extends BasePresenter<ProfilePresenter> implements FBStorageExecutor.ExecuteCallback<Photo>
{
    private static final String TAG = "ProfilePresenter";

    private GalleryStorage galleryStorage;

    public ProfilePresenter(BaseWindow activity) {
        super(activity);
        galleryStorage = ContextApp.getApp(activity.getContext()).getGalleryStorage();
    }

    public void uploadImage(Uri image) {
        PhotoUploader.with(context())
                .callback(this)
                .upload(image);
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
    protected ViewHandler<ProfilePresenter> createViewHandler(BaseWindow<ProfilePresenter> activity) {
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
