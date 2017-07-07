package org.hvkz.hvkz.modules.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.hvkz.hvkz.modules.gallery.images_gallery.ImagesGalleryActivity;

import java.util.ArrayList;
import java.util.List;

public class MessageImagesProvider implements View.OnClickListener
{
    private String[] imageUrls;
    private int position;

    public MessageImagesProvider(String[] urls, int position) {
        this.imageUrls = urls;
        this.position = position;
    }

    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(view.getContext(), ImagesGalleryActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("photos", imageUrls);

        view.getContext().startActivity(intent);
    }

    public static void provide(Context context, List<Photo> urls, int position)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", new ArrayList<>(urls));
        bundle.putInt("position", position);

        Intent intent = new Intent(context, ImagesGalleryActivity.class);
        intent.putExtra("bundle", bundle);

        context.startActivity(intent);
    }
}