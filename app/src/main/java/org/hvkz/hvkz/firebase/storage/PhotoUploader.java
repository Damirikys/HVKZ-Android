package org.hvkz.hvkz.firebase.storage;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.photos.PhotosDb;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.models.ViewBinder;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class PhotoUploader implements FBStorageExecutor.ExecuteCallback<Uri>
{
    private FBStorageExecutor.StorageExecutor storageExecutor;
    private FBStorageExecutor.ExecuteCallback<Photo> callback;
    private Uri uploadedUri;
    private boolean isCanceled;
    private View layout;

    @BindView(R.id.selected_image_view)
    private ImageView imageView;

    @BindView(R.id.img_desc_edit_text)
    private EditText descEditText;

    @BindView(R.id.selected_image_progress)
    private RingProgressBar progressBar;

    private PhotoUploader(Context context) {
        this.storageExecutor = FBStorageExecutor.execute(FBStorageExecutor.PHOTO_DIR, this);
        this.isCanceled = false;

        ViewBinder.handle(this, layout = View.inflate(context, R.layout.image_upload_layout, null));
    }

    public static PhotoUploader with(Context context) {
        return new PhotoUploader(context);
    }

    public PhotoUploader callback(FBStorageExecutor.ExecuteCallback<Photo> callback) {
        this.callback = callback;
        return this;
    }

    public void upload(Uri image) {
        this.imageView.setImageURI(image);
        this.imageView.setAlpha(0.4f);

        final AlertDialog alertDialog = new AlertDialog.Builder(layout.getContext())
                .setView(layout)
                .setPositiveButton("Загрузить", null)
                .setOnCancelListener(dialog -> {
                    if (uploadedUri != null)
                        storageExecutor.delete(uploadedUri);
                    isCanceled = true;
                })
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (uploadedUri == null)
                    Toast.makeText(layout.getContext(), "Подождите, фотография загружается", Toast.LENGTH_SHORT).show();
                else {
                    onCompleteTask(descEditText.getText().toString());
                    dialog.dismiss();
                }
            });
        });

        alertDialog.show();
        storageExecutor.upload(image);
    }

    public void delete(Uri image) {
        storageExecutor.delete(image);
    }

    @Override
    public void onUploaded(Uri uploadedUri) {
        if (isCanceled) {
            storageExecutor.delete(uploadedUri);
        } else {
            this.uploadedUri = uploadedUri;
            this.progressBar.setVisibility(View.GONE);
            this.imageView.setAlpha(1f);
        }
    }

    private void onCompleteTask(String description) {
        Photo photo = new Photo(uploadedUri.toString())
                .setDate(System.currentTimeMillis())
                .setDescription(description);

        PhotosDb.upload(photo, value -> {
            if (value) {
                if (callback != null) callback.onUploaded(photo);
            } else {
                if (callback != null) callback.onFailure(new Exception("Not upload."));
            }
        });
    }

    @Override
    public void onProgress(long value, long max) {
        progressBar.setMax((int) max);
        progressBar.setProgress((int) value);

        if (callback != null) callback.onProgress(value, max);
    }

    @Override
    public void onRemoved() {
        if (callback != null) callback.onRemoved();
    }

    @Override
    public void onFailure(Exception e) {
        if (callback != null) callback.onFailure(e);
    }
}
