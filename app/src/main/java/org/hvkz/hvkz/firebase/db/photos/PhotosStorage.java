package org.hvkz.hvkz.firebase.db.photos;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.interfaces.Callback;

import java.util.LinkedList;
import java.util.List;

public class PhotosStorage
{
    public static final String KEY = "photos";

    private HVKZApp app;
    private DatabaseReference database;

    public PhotosStorage(HVKZApp app){
        this.app = app;
        this.database = FirebaseDatabase.getInstance().getReference(KEY);
        this.database.keepSynced(true);
    }

    public void upload(Photo photo, Callback<Boolean> callback) {
        database
            .child(String.valueOf(app.getCurrentUser().getUserId()))
            .child(String.valueOf(photo.getDate()))
            .setValue(photo)
            .addOnCompleteListener(task -> callback.call(task.isSuccessful()));
    }

    public void remove(Photo photo, Callback<Boolean> callback) {
        database
            .child(String.valueOf(app.getCurrentUser().getUserId()))
            .child(String.valueOf(photo.getDate()))
            .removeValue().addOnCompleteListener(task -> callback.call(task.isSuccessful()));
    }

    public void getAll(Callback<List<Photo>> callback) {
        database
            .child(String.valueOf(app.getCurrentUser().getUserId()))
            .addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Photo> photos = new LinkedList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        photos.add(snapshot.getValue(Photo.class));
                    }

                    callback.call(photos);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
    }
}
