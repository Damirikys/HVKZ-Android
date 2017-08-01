package org.hvkz.hvkz.modules.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.modules.profile.gallery.GalleryExtractor;
import org.hvkz.hvkz.modules.profile.gallery.GalleryViewAdapter;
import org.hvkz.hvkz.modules.profile.gallery.ItemDecorationAlbumColumns;
import org.hvkz.hvkz.modules.profile.gallery.RemoteGalleryExtractor;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uapi.extensions.Parameters;
import org.hvkz.hvkz.utils.ContextApp;

import static org.hvkz.hvkz.modules.NavigationActivity.GALLERY_REQUEST;
import static org.hvkz.hvkz.modules.profile.ProfileFragment.USER_ID;

public class ProfileViewHandler extends ViewHandler<ProfilePresenter>
{
    private User user;

    @BindView(R.id.profileScrollView)
    private NestedScrollView nestedScrollView;

    @BindView(R.id.full_name)
    private TextView displayName;

    @BindView(R.id.group_name)
    private TextView groupName;

    @BindView(R.id.chest_value)
    private TextView chestValue;

    @BindView(R.id.underchest_value)
    private TextView underchestValue;

    @BindView(R.id.waist_value)
    private TextView waistValue;

    @BindView(R.id.pelvis_value)
    private TextView pelvisValue;

    @BindView(R.id.buttocks_value)
    private TextView buttocksValue;

    @BindView(R.id.height_value)
    private TextView heightValue;

    @BindView(R.id.weight_value)
    private TextView weightValue;

    @BindView(R.id.imt_value)
    private TextView imtValue;

    @BindView(R.id.target_weight_value)
    private TextView targetWeightValue;

    @BindView(R.id.ideal_weight_value)
    private TextView idealWeightValue;

    @BindView(R.id.photo)
    private ImageView photo;

    @BindView(R.id.fab)
    private FloatingActionButton floatingActionButton;

    @BindView(R.id.recyclerGalleryView)
    private RecyclerView recyclerView;

    private GalleryExtractor galleryExtractor;
    private GalleryViewAdapter galleryViewAdapter;
    private boolean isViewer;

    ProfileViewHandler(BaseWindow<ProfilePresenter> baseWindow) {
        super(baseWindow);
    }

    @Override
    protected void handle(Context context) {
        HVKZApp hvkzApp = ContextApp.getApp(context);
        user = hvkzApp.getCurrentUser();

        Bundle args = window(Fragment.class).getArguments();
        if (args != null) {
            isViewer = true;
            int userId = args.getInt(USER_ID);
            floatingActionButton.setVisibility(View.GONE);
            hvkzApp.getUsersStorage().getByIdFromRemote(userId, value -> {
                user = value;
                setupProfile(hvkzApp);
            });
        } else {
            setupProfile(hvkzApp);
        }
    }

    private void setupProfile(HVKZApp hvkzApp) {
        setupCard();
        setupGallery();

        nestedScrollView.fullScroll(View.FOCUS_UP);
        galleryExtractor =  (isViewer)
                ? new RemoteGalleryExtractor(user.getUserId(), hvkzApp, galleryViewAdapter)
                : new GalleryExtractor(hvkzApp, galleryViewAdapter);
    }

    GalleryViewAdapter getGalleryViewAdapter() {
        return galleryViewAdapter;
    }

    private void setupGallery() {
        recyclerView.setLayoutManager(new GridLayoutManager(context(), 3));
        recyclerView.addItemDecoration(new ItemDecorationAlbumColumns(2, 3));
        recyclerView.setAdapter(galleryViewAdapter = new GalleryViewAdapter(context()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusable(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if(view.getChildAt(view.getChildCount() - 1) != null) {
                        View window = window(Fragment.class).getView();
                        if (window == null) return;
                        int windowHeight = window.getMeasuredHeight();
                        int childHeight = view.getChildAt(view.getChildCount() - 1).getMeasuredHeight();
                            if (scrollY  >= (childHeight - windowHeight) && scrollY > oldScrollY) {
                                if (!galleryExtractor.isLoading()) galleryExtractor.loadMore();
                            }
                    }
                });
    }


    private void setupCard() {
        Parameters parameters = user.getParameters();
        String height, weight;

        displayName.setText(user.getDisplayName());
        groupName.setText(user.getGroupName());
        chestValue.setText(parameters.getChest());
        underchestValue.setText(parameters.getUnderchest());
        waistValue.setText(parameters.getWaistCirc());
        pelvisValue.setText(parameters.getGirthPelvis());
        buttocksValue.setText(parameters.getGirthButtocks());
        heightValue.setText(height = parameters.getGrowth());
        weightValue.setText(weight = parameters.getWeight());
        targetWeightValue.setText(parameters.getDesiredWeight());

        try {
            double w = Double.valueOf(weight);
            double h = Double.valueOf(height);

            int imt = (int) ((w / (h*h)) * 10000);
            int idealWeight = (int) ((h-100)*0.85);

            imtValue.setText(String.valueOf(imt));
            idealWeightValue.setText(String.valueOf(idealWeight));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Glide.with(window(Fragment.class))
                .load(user.getPhotoUrl())
                .into(photo);
    }

    @OnClick(R.id.fab)
    public void pickPhotoAction(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity().startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }
}
