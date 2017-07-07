package org.hvkz.hvkz.modules.home;

import android.net.Uri;
import android.util.Log;

import org.hvkz.hvkz.app.BasePresenter;
import org.hvkz.hvkz.db.firebase.GalleryStorage;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

public class MainPresenter extends BasePresenter implements FBStorageExecutor.ExecuteCallback<Photo>
{
    private static final String TAG = "MainPresenter";

    private static final int PHOTO_LIMIT = 30;
    private static int PHOTO_OFFSET = PHOTO_LIMIT * (-1);

    private GalleryStorage galleryStorage;
    private FBStorageExecutor.StorageExecutor storageExecutor;
    private ExecutePhotoController executePhotoController;

    public MainPresenter(MainActivity activity) {
        super(activity);
        this.galleryStorage = GalleryStorage.getInstance();
        this.executePhotoController = new ExecutePhotoController(this);
        this.storageExecutor = FBStorageExecutor.execute(FBStorageExecutor.PHOTO_DIR, executePhotoController);
    }

    private void init() {
        if (galleryStorage.isEmpty()) refreshGallery();
        else loadMore();
    }

    public void refreshGallery() {
    }

    public void loadMore() {
//        int oldCount = galleryViewAdapter.getItemCount();
//        galleryViewAdapter.addItems(galleryStorage.getUserPhoto(PHOTO_LIMIT, PHOTO_OFFSET = PHOTO_OFFSET+30));
//
//        if (oldCount == 0)
//            galleryViewAdapter.notifyDataSetChanged();
//        else galleryViewAdapter.notifyItemRangeInserted(oldCount, PHOTO_LIMIT);
    }

    public void uploadImage(Uri image, String description) {
        executePhotoController.changeTask(description);
        storageExecutor.upload(image);
    }

    public void deletePhoto(Photo photo) {
        storageExecutor.delete(Uri.parse(photo.getUrl()));
    }

    public void clearGallery() {
        // TODO
    }

    @Override
    public void onUploaded(Photo uploaded) {
        Log.d(TAG, "SUCCESS UPLOADED");
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
        return new MainViewHandler();
    }
}
