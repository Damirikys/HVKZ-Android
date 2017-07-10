package org.hvkz.hvkz.modules.profile;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.BaseActivity;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.ViewModel;
import org.hvkz.hvkz.modules.profile.gallery.GalleryViewAdapter;
import org.hvkz.hvkz.modules.profile.gallery.ItemDecorationAlbumColumns;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.entities.UserData;

import javax.inject.Inject;

import static org.hvkz.hvkz.modules.MainActivity.GALLERY_REQUEST;

public class ProfileViewHandler implements ViewHandler
{
    @Inject
    User user;

    private GalleryViewAdapter galleryViewAdapter;

    public ProfileViewHandler() {
        HVKZApp.component().inject(this);
    }

    public GalleryViewAdapter getGalleryViewAdapter() {
        return galleryViewAdapter;
    }

    @Override
    public void handle(ViewModel<BaseActivity> viewModel) {
        setupCard(viewModel);
        setupGallery(viewModel);
    }

    private void setupGallery(ViewModel<BaseActivity> viewModel) {
        RecyclerView recyclerView = viewModel.on(RecyclerView.class)
                .with(R.id.recyclerGalleryView);

        recyclerView.setLayoutManager(new GridLayoutManager(viewModel.context(), 3));
        recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(2, 3));
        recyclerView.setAdapter(galleryViewAdapter = new GalleryViewAdapter(viewModel.context()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private void setupCard(ViewModel<BaseActivity> viewModel) {
        UserData userData = user.getUserData();
        String height, weight;

        viewModel.on(TextView.class)
                .with(R.id.full_name)
                .setText(user.getDisplayName());

        viewModel.on(TextView.class)
                .with(R.id.group_name)
                .setText(user.getGroupName());

        viewModel.on(TextView.class)
                .with(R.id.chest_value)
                .setText(userData.getChestCircumference());

        viewModel.on(TextView.class)
                .with(R.id.underchest_value)
                .setText(userData.getUnderChestCircumference());

        viewModel.on(TextView.class)
                .with(R.id.underchest_value)
                .setText(userData.getUnderChestCircumference());

        viewModel.on(TextView.class)
                .with(R.id.waist_value)
                .setText(userData.getField(UserData.WAIST_CIRCUMFERENCE));

        viewModel.on(TextView.class)
                .with(R.id.pelvis_value)
                .setText(userData.getField(UserData.GIRTH_PELVIS));

        viewModel.on(TextView.class)
                .with(R.id.buttocks_value)
                .setText(userData.getField(UserData.GIRTH_BUTTOCKS));

        viewModel.on(TextView.class)
                .with(R.id.height_value)
                .setText(height = userData.getField(UserData.GROWTH));

        viewModel.on(TextView.class)
                .with(R.id.weight_value)
                .setText(weight = userData.getField(UserData.WEIGHT));

        double w = Double.valueOf(weight);
        double h = Double.valueOf(height);

        int imt = (int) ((w / (h*h)) * 10000);
        int idealWeight = (int) ((h-100)*0.85);

        viewModel.on(TextView.class)
                .with(R.id.imt_value)
                .setText(String.valueOf(imt));

        viewModel.on(TextView.class)
                .with(R.id.target_weight_value)
                .setText(userData.getField(UserData.DESIRED_WEIGHT));

        viewModel.on(TextView.class)
                .with(R.id.ideal_weight_value)
                .setText(String.valueOf(idealWeight));


        Glide.with(viewModel.context())
                .load(user.getPhotoUrl())
                .fitCenter()
                .into(viewModel.on(ImageView.class).with(R.id.photo));

        viewModel.on(ScrollView.class)
                .with(R.id.scrollView)
                .getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener()
                {
                    int beforeScrollY = 0;

                    @Override
                    public void onScrollChanged() {
                        int scrollY = viewModel.on(ScrollView.class).with(R.id.scrollView).getScrollY();

                        if (beforeScrollY < scrollY) {
                            viewModel.on(FloatingActionButton.class).with(R.id.fab).hide();
                        } else {
                            viewModel.on(FloatingActionButton.class).with(R.id.fab).show();
                        }

                        beforeScrollY = scrollY;
                    }
                });

        viewModel.on(FloatingActionButton.class)
                .with(R.id.fab)
                .setOnClickListener(v -> {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    ((Activity) viewModel.context())
                            .startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                });
    }
}
