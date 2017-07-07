package org.hvkz.hvkz.utils.network;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public abstract class FBStorageExecutor
{
    public static final String PHOTO_DIR = "photos";

    private static final String TAG = "FBStorageExecutor";

    public static StorageExecutor execute(final String dir, final ExecuteCallback<Uri> callback) {
        return new StorageExecutor(dir, callback);
    }

    public static final class StorageExecutor
    {
        private static StorageReference mStorage;

        private final String dir;
        private ExecuteCallback<Uri> callback;

        private StorageExecutor(String dir, ExecuteCallback<Uri> callback) {
            this.dir = dir;
            this.callback = callback;
            mStorage = FirebaseStorage.getInstance().getReference();
        }

        public void upload(final Uri fileUri) {
            final StorageReference photoRef = mStorage.child(dir)
                    .child(fileUri.getLastPathSegment());

            photoRef.putFile(fileUri)
                    .addOnProgressListener(taskSnapshot ->
                            callback.onProgress(taskSnapshot.getBytesTransferred(), taskSnapshot.getTotalByteCount()))
                    .addOnSuccessListener(taskSnapshot -> {
                        Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                        callback.onUploaded(downloadUri);
                    })
                    .addOnFailureListener(callback::onFailure);
        }

        public void delete(final Uri file) {
            mStorage.child(file.getLastPathSegment())
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onRemoved();
                        } else {
                            callback.onFailure(new Exception("Not removed"));
                        }
                    });
        }
    }

    public interface ExecuteCallback<T> {
        void onUploaded(T uploaded);
        void onRemoved();
        void onFailure(Exception e);
        void onProgress(long value, long max);
    }
}
