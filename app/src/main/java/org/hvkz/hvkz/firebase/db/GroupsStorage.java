package org.hvkz.hvkz.firebase.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.adapters.EventAdapter;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.util.ArrayList;
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
        this.database.addChildEventListener(new EventAdapter() {
            @Override
            public void onDataWasChanged() {
                getMyGroups(REMOTE, EventChannel::send);
            }
        });
    }

    public void createGroup(String groupName, GroupItem groupItem, Callback<Boolean> callback) {
        database.child(groupName)
                .setValue(groupItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        getMyGroups(REMOTE, EventChannel::send);

                    callback.call(task.isSuccessful());
                });
    }

    public void excludeMember(User user, Group group, Callback<Boolean> callback) {
        SparseArray<User> array = group.getMembers();
        List<Integer> members = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            if (key != user.getUserId()) {
                members.add(key);
            }
        }

        database.child(group.getGroupName())
                .child("members")
                .setValue(members)
                .addOnCompleteListener(task -> callback.call(task.isSuccessful()));
    }

    public void getAllFromCache(Callback<List<Group>> callback) {
        app.getOptionsStorage().isAccepted(accepted -> {
            if (accepted) {
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
            } else {
                callback.call(Collections.emptyList());
            }
        });
    }

    public void getAllFromRemote(Callback<List<Group>> callback) {
        app.getOptionsStorage().isAccepted(accepted -> {
            if (accepted) {
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
            } else {
                callback.call(Collections.emptyList());
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
