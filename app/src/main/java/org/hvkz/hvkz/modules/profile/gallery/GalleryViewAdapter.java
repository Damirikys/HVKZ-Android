package org.hvkz.hvkz.modules.profile.gallery;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.db.photos.PhotosDb;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.modules.gallery.ImagesProvider;

import java.util.ArrayList;
import java.util.List;

public class GalleryViewAdapter extends RecyclerView.Adapter<GalleryViewAdapter.GalleryViewHolder>
{
    private List<Photo> items;
    private Context context;

    public GalleryViewAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void addPhotos(List<Photo> photos) {
        this.items.addAll(photos);
    }

    public GalleryViewAdapter addPhoto(Photo photo) {
        items.add(0, photo);
        return this;
    }

    public void removePhoto(int position)
    {
        items.remove(position);
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
                .centerCrop()
                .placeholder(R.drawable.imgplaceholder)
                .into(holder.photo_item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public ImageView photo_item;

        public GalleryViewHolder(View itemView) {
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
            builder.setTitle("Выберите действие")
                    .setItems(new String[]{"Удалить"}, (dialog, which) ->
                    {
                        PhotosDb.remove(photo, result -> {
                            if (result) {
                                GalleryStorage.getInstance().remove(photo);
                                items.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                            } else {
                                new AlertDialog.Builder(context)
                                        .setMessage("Не удалось удалить фотографию.")
                                        .create()
                                        .show();
                            }
                        });

                        dialog.dismiss();
                    });

            builder.create().show();
            return true;
        }
    }
}
