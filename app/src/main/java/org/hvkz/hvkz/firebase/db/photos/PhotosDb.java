package org.hvkz.hvkz.firebase.db.photos;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Photo;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class PhotosDb
{
    private static final PhotosDb PHOTOS_DB = new PhotosDb();

    public static final String KEY = "photos";

    private DatabaseReference database;

    @Inject
    User user;

    private PhotosDb(){
        HVKZApp.component().inject(this);
        database = FirebaseDatabase.getInstance().getReference(KEY);
    }

    public static void uploadPhoto(Photo photo, Callback<Boolean> callback) {
        PHOTOS_DB.database
                .child(String.valueOf(PHOTOS_DB.user.getUserId()))
                .child(String.valueOf(photo.getDate()))
                .setValue(photo)
                .addOnCompleteListener(task -> callback.call(task.isSuccessful()));
    }

    public static void deletePhoto() {

    }

    public static void getAll(Callback<List<Photo>> callback) {
        PHOTOS_DB.database
                .child(String.valueOf(PHOTOS_DB.user.getUserId()))
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

    public static void lol() {
        PHOTOS_DB.database
                .child(String.valueOf(PHOTOS_DB.user.getUserId()))
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        System.out.println("CHILD ADDDEEEED");
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
