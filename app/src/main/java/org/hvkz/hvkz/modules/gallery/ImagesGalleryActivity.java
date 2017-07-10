package org.hvkz.hvkz.modules.gallery;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.entities.Photo;

import java.util.ArrayList;
import java.util.List;

public class ImagesGalleryActivity extends AppCompatActivity
{
    private List<Photo> photos;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.images_gallery);
        dataInitialize();
        setConfig();
    }

    private void dataInitialize() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null)
        {
            this.position = bundle.getInt("position");
            this.photos = (List<Photo>) bundle.getSerializable("photos");
        }
        else
        {
            this.position = getIntent().getIntExtra("position", 0);
            this.photos = new ArrayList<>();

            String[] urls = getIntent().getStringArrayExtra("photos");
            for (String url : urls)
                photos.add(new Photo(url));
        }
    }

    private void setConfig()
    {
        FragmentPagerAdapter adapterViewPager =
                new ImagePagerAdapter(getSupportFragmentManager())
                        .setData(photos);

        GalleryPager vpPager = (GalleryPager) findViewById(R.id.vpPager);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(position);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .sensitivity(0.5f)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(1000)
                .distanceThreshold(0.1f)
                .edge(true)
                .edgeSize(1f)
                .build();

        Slidr.attach(this, config);
    }


    public static class ImagePagerAdapter extends FragmentPagerAdapter
    {
        List<ImageFragment> fragments;

        ImagePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragments = new ArrayList<>();
        }

        public ImagePagerAdapter setData(List<Photo> photos)
        {
            for (int i = 0; i < photos.size(); i++)
                fragments.add(ImageFragment.newInstance(
                        photos.get(i).getUrl(), photos.get(i).getDescription()));

            return this;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position)
        {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }
}