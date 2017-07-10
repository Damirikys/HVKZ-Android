package org.hvkz.hvkz.firebase.db.users;

import android.util.SparseArray;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersDb
{
    private static final UsersDb USERS_DB = new UsersDb();

    public static final String KEY = "users";
    public static final String NAME_KEY = "name";
    public static final String PHOTO_KEY = "photo";
    public static final String DEVICES_KEY = "devices";
    public static final String ANDROID = "android";

    private DatabaseReference database;

    private UsersDb(){
        database = FirebaseDatabase.getInstance().getReference(KEY);
    }

    public static void update() {
        User user = UAPIUser.getUAPIUser();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(ANDROID, FirebaseInstanceId.getInstance().getToken());

        data.put(DEVICES_KEY, tokenMap);
        data.put(NAME_KEY, user.getDisplayName());
        data.put(PHOTO_KEY, user.getPhotoUrl().toString());

        USERS_DB.database.child(String.valueOf(user.getUserId()))
                .updateChildren(data);
    }

    public static void getById(int userId, Callback<User> callback) {
        USERS_DB.database.child(String.valueOf(userId))
                .addListenerForSingleValueEvent(new UserDbReceiver(callback));
    }

    @SuppressWarnings("unchecked")
    public static void getProfiles(List<Integer> ids, Callback<SparseArray<User>> callback) {
        SparseArray<User> sparseArray = new SparseArray<>();

        for (Integer id : ids) {
            getById(id, value -> {
                sparseArray.put(id, value);
                if (sparseArray.size() == ids.size()) {
                    callback.call(sparseArray);
                }
            });
        }
    }
}
