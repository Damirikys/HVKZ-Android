package org.hvkz.hvkz.modules.gallery.images_gallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hvkz.hvkz.R;

import uk.co.senab.photoview.PhotoView;

public class ImageFragment extends Fragment
{
    private String url, descr;

    public static ImageFragment newInstance(String url, String description)
    {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("desc", description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString("url");
        descr = getArguments().getString("desc");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        PhotoView image = (PhotoView) view.findViewById(R.id.fragmentimage);
        TextView description = (TextView) view.findViewById(R.id.description);
        description.setText(descr);

        Picasso.with(getContext())
                .load(url)
                .into(image);

        return view;
    }
}
