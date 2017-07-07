package org.hvkz.hvkz.modules.home;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.app.BaseActivity;
import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.entities.UserData;

import javax.inject.Inject;

public class MainViewHandler implements ViewHandler
{
    @Inject
    User user;

    public MainViewHandler() {
        HVKZApp.component().inject(this);
    }

    @Override
    public void handle(ViewModel<BaseActivity> viewModel) {
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


        Picasso.with(viewModel.context())
                .load(user.getPhotoUrl())
                .into(viewModel.on(ImageView.class).with(R.id.photo));

        RecyclerView recyclerView = viewModel.on(RecyclerView.class)
                .with(R.id.recyclerGalleryView);

        recyclerView.setLayoutManager(new GridLayoutManager(viewModel.context(), 3));
        recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(2, 3));
        recyclerView.setAdapter(new GalleryViewAdapter(viewModel.context()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
