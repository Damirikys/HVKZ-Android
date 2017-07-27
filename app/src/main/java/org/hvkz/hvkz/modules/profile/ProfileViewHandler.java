package org.hvkz.hvkz.modules.profile;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.firebase.db.photos.PhotosStorage;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.modules.profile.gallery.GalleryViewAdapter;
import org.hvkz.hvkz.modules.profile.gallery.ItemDecorationAlbumColumns;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.entities.UserData;
import org.hvkz.hvkz.utils.ContextApp;

import javax.inject.Inject;

import static org.hvkz.hvkz.modules.MainActivity.GALLERY_REQUEST;

public class ProfileViewHandler extends ViewHandler<ProfilePresenter>
{
    private static final String TAG = "ProfileViewHandler";

    private final int PHOTO_LIMIT = 15;
    private int PHOTO_OFFSET = PHOTO_LIMIT * (-1);

    @Inject
    User user;

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

    @BindView(R.id.recyclerGalleryView)
    private RecyclerView recyclerView;

    private PhotosStorage photosStorage;
    private GalleryStorage galleryStorage;
    private GalleryViewAdapter galleryViewAdapter;
    private boolean isLoading;

    public ProfileViewHandler(BaseWindow<ProfilePresenter> baseWindow) {
        super(baseWindow);
        if (galleryStorage.isEmpty()) refreshGallery();
        else loadMore();
    }

    @Override
    protected void handle(Context context) {
        HVKZApp hvkzApp = ContextApp.getApp(context);
        hvkzApp.component().inject(this);

        photosStorage = hvkzApp.getPhotosStorage();
        galleryStorage = hvkzApp.getGalleryStorage();

        setupCard();
        setupGallery();

        nestedScrollView.fullScroll(View.FOCUS_UP);
    }

    public GalleryViewAdapter getGalleryViewAdapter() {
        return galleryViewAdapter;
    }

    public void loadMore() {
        Log.d(TAG, "Load more from DB");
        isLoading = true;
        int oldCount = galleryViewAdapter.getItemCount();
        galleryViewAdapter.addPhotos(galleryStorage.getPhotos(PHOTO_LIMIT, PHOTO_OFFSET = PHOTO_OFFSET + PHOTO_LIMIT));

        if (oldCount == 0) galleryViewAdapter.notifyDataSetChanged();
        else galleryViewAdapter.notifyItemRangeInserted(oldCount, PHOTO_LIMIT);
        isLoading = false;
    }

    public void refreshGallery() {
        Log.d(TAG, "Load from Firebase Storage.");
        photosStorage.getAll(photos -> {
            galleryStorage.clear();
            galleryViewAdapter.clear();

            galleryStorage.addAll(photos);
            PHOTO_OFFSET = PHOTO_LIMIT * (-1);
            loadMore();
        });
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
                        if ((scrollY  >= (view.getChildAt(view.getChildCount() - 1).getMeasuredHeight()
                                - window(Fragment.class).getView().getMeasuredHeight())) &&
                                scrollY > oldScrollY)
                        {
                            if (!isLoading) loadMore();
                        }
                    }
                });
    }


    private void setupCard() {
        UserData userData = user.getUserData();
        String height, weight;

        displayName.setText(user.getDisplayName());
        groupName.setText(user.getGroupName());
        chestValue.setText(userData.getChestCircumference());
        underchestValue.setText(userData.getUnderChestCircumference());
        waistValue.setText(userData.getField(UserData.WAIST_CIRCUMFERENCE));
        pelvisValue.setText(userData.getField(UserData.GIRTH_PELVIS));
        buttocksValue.setText(userData.getField(UserData.GIRTH_BUTTOCKS));
        heightValue.setText(height = userData.getField(UserData.GROWTH));
        weightValue.setText(weight = userData.getField(UserData.WEIGHT));

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

        targetWeightValue.setText(userData.getField(UserData.DESIRED_WEIGHT));

        Glide.with(window(Fragment.class))
                .load(user.getPhotoUrl())
                .into(photo);
    }

    @OnClick(R.id.fab)
    public void pickPhotoAction() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity().startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }
}
