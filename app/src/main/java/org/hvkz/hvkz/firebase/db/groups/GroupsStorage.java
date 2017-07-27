package org.hvkz.hvkz.firebase.db.groups;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GroupsStorage
{
    public static final String KEY = "groups";
    public static final int LOCAL = 1;
    public static final int REMOTE = 2;

    private HVKZApp app;
    private SharedPreferences preferences;
    private DatabaseReference database;

    public GroupsStorage(HVKZApp app) {
        this.app = app;
        this.database = FirebaseDatabase.getInstance().getReference(KEY);
        this.preferences = app.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        this.database.keepSynced(true);
    }

    public void getAllFromCache(Callback<List<Group>> callback) {
        List<Group> groups = new LinkedList<>();
        Set<String> keySet = preferences.getAll().keySet();
        if (keySet.size() == 0) {
            getAllFromRemote(callback);
            return;
        }

        for (String key : preferences.getAll().keySet()) {
            GroupItem groupItem = JSONFactory.fromJson(preferences.getString(key, ""), GroupItem.class);
            app.getUsersStorage().getProfilesFromCache(groupItem.members, value -> {
                groups.add(new Group()
                        .setGroupName(key)
                        .setMembers(value)
                        .setAdmin(value.get(groupItem.admin))
                        .setNotice(groupItem.notice)
                );

                if (groups.size() == keySet.size()) {
                    callback.call(groups);
                }
            });
        }
    }

    public void getAllWithUpdates(Callback<List<Group>> callback) {
        getAllFromCache(value -> {
            callback.call(value);
            getAllFromRemote(callback);
        });
    }

    public void getAllFromRemote(Callback<List<Group>> callback) {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> groups = new LinkedList<>();
                preferences.edit().clear().apply();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    GroupItem groupItem = snapshot.getValue(GroupItem.class);
                    if (groupItem != null) {
                        groupItem.members.removeAll(Collections.singleton(null));

                        preferences.edit()
                                .putString(snapshot.getKey(), JSONFactory.toJson(groupItem))
                                .apply();

                        app.getUsersStorage().getProfilesFromRemote(groupItem.members, members -> {
                            groups.add(new Group()
                                    .setGroupName(snapshot.getKey())
                                    .setMembers(members)
                                    .setAdmin(members.get(groupItem.admin))
                                    .setNotice(groupItem.notice));

                            if (groups.size() == dataSnapshot.getChildrenCount()) {
                                callback.call(groups);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    public void getMyGroups(int from, Callback<List<Group>> callback) {
        int userId = app.getCurrentUser().getUserId();
        getUserGroups(from, userId, callback);
    }

    public void getUserGroups(int from, int userId, Callback<List<Group>> callback) {
        if (from == LOCAL) {
            getAllFromCache(value -> callback.call(findUserGroups(userId, value)));
        } else {
            getAllFromRemote(value -> callback.call(findUserGroups(userId, value)));
        }
    }

    public void getGroupByName(String name, Callback<Group> callback) {
        getAllFromCache(value -> {
            for (Group group : value) {
                if (name.equals(group.getGroupName())) {
                    callback.call(group);
                    return;
                }
            }
        });
    }

    private List<Group> findUserGroups(int userId, List<Group> groups) {
        List<Group> result = new LinkedList<>();
        for (Group group : groups) {
            User member = group.getMembers().get(userId);
            if (member != null) result.add(group);
        }

        return result;
    }

    public static class GroupItem {
        public int admin;
        public String notice;
        public List<Integer> members;
    }
}
