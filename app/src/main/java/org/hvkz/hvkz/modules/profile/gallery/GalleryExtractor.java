package org.hvkz.hvkz.modules.profile.gallery;

import android.util.Log;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.db.PhotosStorage;

public class GalleryExtractor
{
    private static final String TAG = "GalleryExtractor";

    final int PHOTO_LIMIT = 15;
    int PHOTO_OFFSET = PHOTO_LIMIT * (-1);

    PhotosStorage photosStorage;
    private GalleryStorage galleryStorage;
    protected GalleryViewAdapter adapter;
    boolean isLoading;

    public GalleryExtractor(HVKZApp hvkzApp, GalleryViewAdapter adapter) {
        this.photosStorage = hvkzApp.getPhotosStorage();
        this.galleryStorage = hvkzApp.getGalleryStorage();
        this.adapter = adapter;
        init();
    }

    protected void init() {
        if (galleryStorage.isEmpty()) refreshGallery();
        else loadMore();
    }

    public void loadMore() {
        Log.d(TAG, "Load more from DB");
        isLoading = true;
        int oldCount = adapter.getItemCount();
        adapter.addPhotos(galleryStorage.getPhotos(PHOTO_LIMIT, PHOTO_OFFSET = PHOTO_OFFSET + PHOTO_LIMIT));

        if (oldCount == 0) adapter.notifyDataSetChanged();
        else adapter.notifyItemRangeInserted(oldCount, PHOTO_LIMIT);
        isLoading = false;
    }

    public void refreshGallery() {
        Log.d(TAG, "Load from Firebase Storage.");
        photosStorage.getAll(photos -> {
            galleryStorage.clear();
            adapter.clear();

            galleryStorage.addAll(photos);
            PHOTO_OFFSET = PHOTO_LIMIT * (-1);
            loadMore();
        });
    }

    public boolean isLoading() {
        return isLoading;
    }
}
