package org.hvkz.hvkz.modules.chats.window;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.eralp.circleprogressview.CircleProgressView;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.interfaces.Thenable;
import org.hvkz.hvkz.utils.network.ExecuteCallbackAdapter;
import org.hvkz.hvkz.utils.network.FBStorageExecutor;

import java.util.ArrayList;
import java.util.List;

public class PhotoAttachmentsExecutor extends ExecuteCallbackAdapter<Uri> implements Destroyable
{
    private static final String TAG = "PhotoAttachments";

    private FBStorageExecutor.StorageExecutor executor;
    private List<Uri> photoUrls;
    private final Handler handler;
    private int uploadCounter;

    public PhotoAttachmentsExecutor() {
        this.photoUrls = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.executor = FBStorageExecutor.execute(FBStorageExecutor.PHOTO_DIR, this);
    }

    public void upload(Uri uri, CircleProgressView progressView, ImageView imageView, Callback<Uri> onLongClick) {
        uploadCounter++;

        new UploadTask(uri, progressView, imageView, handler, new Thenable<Uri>()
        {
            @Override
            public void onSuccess(Uri response) {
                Log.d(TAG, "onUpload " + response.toString());
                if (photoUrls == null || uploadCounter == 0) {
                    Log.d(TAG, "But it was deleted =(");
                    executor.delete(response);
                } else {
                    uploadCounter--;
                    photoUrls.add(response);
                    imageView.setOnLongClickListener(v -> {
                        handler.post(() -> onLongClick.call(response));
                        return false;
                    });
                }
            }

            @Override
            public void onFailed(Throwable t) {
                t.printStackTrace();
                Toast.makeText(imageView.getContext(), "Не удалось загрузить фотографию", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    public int getUploadCounter() {
        return uploadCounter;
    }

    public List<Uri> getAttachedPhotos() {
        return photoUrls;
    }

    public void delete(Uri uri) {
        Log.d(TAG, "delete " + uri.toString());
        photoUrls.remove(uri);
        executor.delete(uri);
    }

    public void clear() {
        photoUrls.clear();
        uploadCounter = 0;
    }

    @Override
    public void onDestroy() {
        for (Uri uri : photoUrls)
            executor.delete(uri);
        photoUrls = null;
    }

    private static class UploadTask extends Thread implements FBStorageExecutor.ExecuteCallback<Uri>
    {
        private FBStorageExecutor.StorageExecutor executor;
        private Handler handler;

        private Uri uri;
        private CircleProgressView progressView;
        private ImageView imageView;
        private Thenable<Uri> thenable;

        private UploadTask(Uri uri,
                           CircleProgressView progressView,
                           ImageView imageView,
                           Handler handler,
                           Thenable<Uri> thenable) {
            this.uri = uri;
            this.progressView = progressView;
            this.thenable = thenable;
            this.imageView = imageView;
            this.handler = handler;
            this.executor = FBStorageExecutor.execute(FBStorageExecutor.PHOTO_DIR, this);
        }

        @Override
        public void run() {
            super.run();
            executor.upload(uri);
            while (!isInterrupted()) { SystemClock.sleep(500); }
        }

        @Override
        public void onUploaded(Uri uploaded) {
            thenable.onSuccess(uploaded);
            handler.post(() -> {
                progressView.animate().scaleX(0);
                imageView.animate().alpha(1f);
            });

            interrupt();
        }

        @Override
        public void onRemoved() {
            interrupt();
        }

        @Override
        public void onFailure(Exception e) {
            thenable.onFailed(e);
            interrupt();
        }

        @Override
        public void onProgress(long value, long max) {
            handler.post(() ->
                    progressView.setProgressWithAnimation(((float) value / max) * 100, 200)
            );
        }
    }
}
