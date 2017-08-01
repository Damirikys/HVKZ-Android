package org.hvkz.hvkz.firebase.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uapi.entities.UnknownUser;
import org.hvkz.hvkz.uapi.entities.UserEntity;
import org.hvkz.hvkz.uapi.extensions.TokensAgent;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersStorage
{
    public static final String KEY = "users";
    public static final String DATA_KEY = "data";
    public static final String DEVICES_KEY = "devices";
    public static final String ANDROID = "android";

    private SharedPreferences preferences;
    private DatabaseReference database;

    public UsersStorage(Context context) {
        database = FirebaseDatabase.getInstance().getReference(KEY);
        preferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        database.keepSynced(true);
    }

    public void update(User user) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(ANDROID, FirebaseInstanceId.getInstance().getToken());

        data.put(DEVICES_KEY, tokenMap);
        data.put(DATA_KEY, JSONFactory.toJson(user));

        database.child(String.valueOf(user.getUserId()))
                .updateChildren(data);
    }

    public void getByIdFromCache(int userId, Callback<User> callback) {
        String cachedData = preferences.getString(String.valueOf(userId), "");
        if (!cachedData.isEmpty()) {
            User user = JSONFactory.fromJson(cachedData, UserEntity.class);
            callback.call(user);
        } else {
            getByIdFromRemote(userId, callback);
        }
    }

    public synchronized void getByIdFromRemote(int userId, Callback<User> callback) {
        database.child(String.valueOf(userId))
            .addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        UserItem fbUser = dataSnapshot.getValue(UserItem.class);
                        UserEntity entity = JSONFactory.fromJson(fbUser.data, UserEntity.class);
                        entity.setDevices(fbUser.devices);
                        preferences.edit().putString(String.valueOf(userId), fbUser.data).apply();
                        callback.call(entity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.call(new UnknownUser());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.call(new UnknownUser());
                }
            });
    }

    public void getProfilesFromCache(List<Integer> ids, Callback<SparseArray<User>> callback) {
        SparseArray<User> sparseArray = new SparseArray<>();

        for (Integer id : ids) {
            getByIdFromCache(id, value -> {
                sparseArray.put(id, value);
                if (sparseArray.size() == ids.size()) {
                    callback.call(sparseArray);
                }
            });
        }
    }

    public synchronized void getProfilesFromRemote(List<Integer> ids, Callback<SparseArray<User>> callback) {
        final SparseArray<User> sparseArray = new SparseArray<>();
        for (Integer id : ids) {
            getByIdFromRemote(id, value -> {
                sparseArray.put(id, value);
                if (sparseArray.size() == ids.size()) {
                    callback.call(sparseArray);
                }
            });
        }
    }

    public synchronized void getUsersDatabase(Callback<List<User>> callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                preferences.edit().clear().apply();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String json = snapshot.child("data").getValue(String.class);
                        UserEntity user = JSONFactory.fromJson(json, UserEntity.class);
                        preferences.edit().putString(String.valueOf(user.getUserId()), JSONFactory.toJson(user)).apply();
                        users.add(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                callback.call(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private static class UserItem {
        public String data;
        public TokensAgent devices;
    }
}
