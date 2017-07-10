package org.hvkz.hvkz.firebase.db.users;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.TokenManager;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.entities.UserProfile;

public class UserDbReceiver implements ValueEventListener
{
    private static final String TAG = "UserDbReceiver";
    private Callback<User> callback;

    public UserDbReceiver(Callback<User> callback) {
        this.callback = callback;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        UserModel fbUser = dataSnapshot.getValue(UserModel.class);
        callback.call(new UserProfile(fbUser.getName(), fbUser.getPhoto(), fbUser.getDevices()));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, databaseError.getMessage() + " : " + databaseError.getDetails());
    }

    private static class UserModel
    {
        private String name;
        private String photo;
        private TokenManager devices;

        public String getName() {
            return name;
        }

        public String getPhoto() {
            return photo;
        }

        public TokenManager getDevices() {
            return devices;
        }
    }
}
