package org.hvkz.hvkz.modules.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.hvkz.hvkz.firebase.entities.Photo;

import java.util.ArrayList;
import java.util.List;

public class ImagesProvider implements View.OnClickListener
{
    private String[] imageUrls;
    private int position;

    public ImagesProvider(String[] urls, int position) {
        this.imageUrls = urls;
        this.position = position;
    }

    public ImagesProvider(List<String> urls, int position) {
        this.imageUrls = new String[urls.size()];
        this.imageUrls = urls.toArray(imageUrls);
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