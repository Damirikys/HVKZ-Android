package org.hvkz.hvkz.modules.profile;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.db.photos.PhotosDb;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.firebase.storage.PhotoUploader;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.modules.profile.gallery.GalleryViewAdapter;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

import static android.app.Activity.RESULT_OK;
import static org.hvkz.hvkz.modules.MainActivity.GALLERY_REQUEST;

public class ProfilePresenter extends BasePresenter implements FBStorageExecutor.ExecuteCallback<Photo>
{
    private static final String TAG = "ProfilePresenter";

    private final int PHOTO_LIMIT = 30;
    private int PHOTO_OFFSET = PHOTO_LIMIT * (-1);

    private ProfileViewHandler viewHandler;
    private GalleryStorage galleryStorage;

    public ProfilePresenter(ProfileFragment fragment) {
        super(fragment);
        this.galleryStorage = GalleryStorage.getInstance();
        if (galleryStorage.isEmpty()) refreshGallery();
        else loadMore();
    }

    public void refreshGallery() {
        Log.d(TAG, "Load from Firebase Storage.");
        PhotosDb.getAll(photos -> {
            galleryStorage.clear();
            viewHandler.getGalleryViewAdapter().clear();
            galleryStorage.addAll(photos);
            PHOTO_OFFSET = PHOTO_LIMIT * (-1);
            loadMore();
        });
    }

    public void uploadImage(Uri image) {
        PhotoUploader.with(getContext())
                .callback(this)
                .execute(image);
    }

    public void loadMore() {
        Log.d(TAG, "Load more from DB");
        GalleryViewAdapter adapter = viewHandler.getGalleryViewAdapter();
        int oldCount = adapter.getItemCount();
        adapter.addPhotos(galleryStorage.getPhotos(PHOTO_LIMIT, PHOTO_OFFSET = PHOTO_OFFSET + PHOTO_LIMIT));

        if (oldCount == 0) adapter.notifyDataSetChanged();
        else adapter.notifyItemRangeInserted(oldCount, PHOTO_LIMIT);
    }

    @Override
    public void onUploaded(Photo uploaded) {
        Log.d(TAG, "SUCCESS UPLOADED");
        viewHandler.getGalleryViewAdapter().addPhoto(uploaded);
        viewHandler.getGalleryViewAdapter().notifyItemInserted(0);
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
    protected ViewHandler getViewHandler() {
        return viewHandler = new ProfileViewHandler();
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
