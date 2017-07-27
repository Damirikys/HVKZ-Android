package org.hvkz.hvkz.firebase.db;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MenuStorage
{
    private static final String KEY = "menu";
    private static final String CACHE_PATH = "menu.json";
    private static final int DAYS_IN_WEEK = 7;

    private HVKZApp app;
    private DatabaseReference database;
    private File menuFile;

    public MenuStorage(HVKZApp app) {
        this.app = app;
        this.database = FirebaseDatabase.getInstance().getReference(KEY);
        this.menuFile = new File(app.getCacheDir(), CACHE_PATH);
        this.database.keepSynced(true);
    }

    public List<MenuItem> getAllFromCache() {
        int length = (int) menuFile.length();
        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(menuFile);
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONFactory.fromJson(new String(bytes), new TypeToken<List<MenuItem>>(){}.getType());
    }

    public List<MenuItem> getWeeklyMenu() {
        List<MenuItem> data = getAllFromCache();
        List<MenuItem> weekly = new ArrayList<>();

        User user = app.getCurrentUser();
        user.getRegTimestamp();

        long difference = (System.currentTimeMillis() / 1000) - user.getRegTimestamp();
        int regday  = (int) (difference / (24 * 60 * 60));

        int today = regday % data.size();
        if (today == 0) today = data.size();
        int dayCount = DAYS_IN_WEEK;

        for (int i = today; i < today + dayCount; i++) {
            if (i >= data.size()) {
                dayCount = (today + dayCount) - data.size();
                today = 0;
                i = 0;
            }

            weekly.add(data.get(i));
        }

        return weekly;
    }

    public void getAllFromRemote(Callback<List<MenuItem>> callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<MenuItem> data = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    data.add(snapshot.getValue(MenuItem.class));
                }

                saveMenuCache(data);
                callback.call(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    private void saveMenuCache(List<MenuItem> data) {
        try {
            FileOutputStream stream = new FileOutputStream(menuFile);
            stream.write(JSONFactory.toJson(data).getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MenuItem {
        public List<Integer> bzu;
        public List<String> dayArray;
    }
}
