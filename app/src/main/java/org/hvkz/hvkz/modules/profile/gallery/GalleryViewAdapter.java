package org.hvkz.hvkz.modules.profile.gallery;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.db.PhotosStorage;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.firebase.storage.PhotoUploader;
import org.hvkz.hvkz.uimodels.gallery.ImagesProvider;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.ExecuteCallbackAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewAdapter extends RecyclerView.Adapter<GalleryViewAdapter.GalleryViewHolder>
{
    private PhotosStorage photosStorage;
    private GalleryStorage galleryStorage;
    private List<Photo> items;
    private Context context;

    public GalleryViewAdapter(Context context) {
        this.context = context;
        this.photosStorage = ContextApp.getApp(context).getPhotosStorage();
        this.galleryStorage = ContextApp.getApp(context).getGalleryStorage();
        this.items = new ArrayList<>();
    }

    void addPhotos(List<Photo> photos) {
        this.items.addAll(photos);
    }

    public GalleryViewAdapter addPhoto(Photo photo) {
        items.add(0, photo);
        return this;
    }

    public void clear()
    {
        items.clear();
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);

        return new GalleryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        Glide.with(context)
                .load(items.get(position).getUrl())
                .placeholder(R.drawable.imgplaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.photo_item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        ImageView photo_item;

        GalleryViewHolder(View itemView) {
            super(itemView);

            photo_item = (ImageView) itemView.findViewById(R.id.photo_item);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ImagesProvider.provide(context, items, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            Photo photo = items.get(getAdapterPosition());

            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.select_action)
                    .setItems(new String[]{context.getString(R.string.delete)}, (dialog, which) -> PhotoUploader.with(context)
                            .callback(new ExecuteCallbackAdapter<Photo>() {
                                @Override
                                public void onRemoved() {
                                    photosStorage.remove(photo, result -> {
                                        if (result) {
                                            galleryStorage.remove(photo);
                                            items.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                        } else {
                                            new AlertDialog.Builder(context)
                                                    .setMessage(R.string.cant_remove_photo)
                                                    .create()
                                                    .show();
                                        }
                                    });

                                    dialog.dismiss();
                                }
                            })
                            .delete(Uri.parse(photo.getUrl())));

            builder.create().show();
            return true;
        }
    }
}
