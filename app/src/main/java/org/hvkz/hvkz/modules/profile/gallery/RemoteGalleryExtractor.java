package org.hvkz.hvkz.modules.profile.gallery;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Photo;

import java.util.ArrayList;
import java.util.List;

public class RemoteGalleryExtractor extends GalleryExtractor
{
    private List<Photo> data;
    private int userId;

    public RemoteGalleryExtractor(int userId, HVKZApp hvkzApp, GalleryViewAdapter adapter) {
        super(hvkzApp, adapter);
        this.userId = userId;
        refreshGallery();
    }

    @Override
    protected void init() {}

    @Override
    public void loadMore() {
        isLoading = true;
        int oldCount = adapter.getItemCount();
        adapter.addPhotos(extract(PHOTO_OFFSET = PHOTO_OFFSET + PHOTO_LIMIT));

        if (oldCount == 0) adapter.notifyDataSetChanged();
        else adapter.notifyItemRangeInserted(oldCount, PHOTO_LIMIT);
        isLoading = false;
    }

    @Override
    public void refreshGallery() {
        photosStorage.getAll(userId, value -> {
            data = value;
            adapter.clear();
            loadMore();
        });
    }

    private List<Photo> extract(int offset) {
        List<Photo> extracted = new ArrayList<>();
        int size = PHOTO_LIMIT + offset;
        if (size > data.size()) size = data.size();
        for (int i = offset; i < size; i++) {
            extracted.add(data.get(i));
        }

        return extracted;
    }
}
