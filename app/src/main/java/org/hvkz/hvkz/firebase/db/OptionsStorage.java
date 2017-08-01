package org.hvkz.hvkz.firebase.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.util.List;
import java.util.Map;

public class OptionsStorage
{
    public static final String KEY = "options";

    private HVKZApp app;
    private SharedPreferences preferences;
    private DatabaseReference database;

    public OptionsStorage(HVKZApp app) {
        this.app = app;
        this.database = FirebaseDatabase.getInstance().getReference(KEY);
        this.preferences = app.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        this.database.keepSynced(true);
    }

    public void isAccepted(Callback<Boolean> callback) {
        getOptions(options -> callback.call(isAccepted(options)));
    }

    private boolean isAccepted(Options options) {
        User user = app.getCurrentUser();
        Integer duration = options.acceptedDuration.get(String.valueOf(user.getGroupId()));
        if (duration != null) {
            if (duration == 0) return true;
            if (user.getActivatedTimestamp() < 0) return false;

            long difference = Tools.timestamp() - Tools.timestamp(user.getActivatedTimestamp());
            int daysFromActivated  = (int) (difference / (24 * 60 * 60));

            return daysFromActivated <= duration;
        } else {
            return false;
        }
    }

    public void isModerated(Callback<Boolean> callback) {
        getOptions(options -> callback.call(isModerated(options)));
    }

    private boolean isModerated(Options options) {
        return options.moderators.contains(app.getCurrentUser().getGroupId());
    }

    public void getOptions(Callback<Options> callback) {
        if (preferences.contains(KEY)) {
            callback.call(JSONFactory.fromJson(preferences.getString(KEY, ""), Options.class));
        } else {
            getOptionsFromRemote(callback);
        }
    }

    public void getOptionsFromRemote(Callback<Options> callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Options options = dataSnapshot.getValue(Options.class);
                preferences.edit().putString(KEY, JSONFactory.toJson(options)).apply();
                callback.call(dataSnapshot.getValue(Options.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static class Options {
        public Map<String, Integer> acceptedDuration;
        public List<Integer> moderators;
        public int support;
    }
}
