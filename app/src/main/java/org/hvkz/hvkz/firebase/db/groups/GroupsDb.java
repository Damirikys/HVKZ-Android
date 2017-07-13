package org.hvkz.hvkz.firebase.db.groups;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.firebase.db.users.UsersDb;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class GroupsDb
{
    private static final GroupsDb GROUPS_DB = new GroupsDb();

    public static final String KEY = "groups";

    private List<Group> allGroups;
    private DatabaseReference database;

    @Inject
    User user;

    private GroupsDb() {
        HVKZApp.component().inject(this);
        database = FirebaseDatabase.getInstance().getReference(KEY);
    }

    public static void getAll(Callback<List<Group>> callback) {
        if (GROUPS_DB.allGroups != null) {
            callback.call(GROUPS_DB.allGroups);
            return;
        }

        GROUPS_DB.database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> groups = new LinkedList<>();

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    GroupItem groupItem = snapshot.getValue(GroupItem.class);

                    UsersDb.getProfiles(groupItem.members, value -> {
                        groups.add(
                                new Group()
                                        .setGroupName(snapshot.getKey())
                                        .setMembers(value)
                                        .setAdmin(value.get(groupItem.admin))
                                        .setNotice(groupItem.notice)
                        );

                        if (groups.size() == dataSnapshot.getChildrenCount()) {
                            GROUPS_DB.allGroups = groups;
                            callback.call(GROUPS_DB.allGroups);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    public static void getMyGroups(Callback<List<Group>> callback) {
        if (GROUPS_DB.allGroups != null) {
            callback.call(findMyGroups(GROUPS_DB.allGroups));
        } else {
            getAll(value -> callback.call(findMyGroups(value)));
        }
    }

    private static List<Group> findMyGroups(List<Group> groups) {
        List<Group> result = new LinkedList<>();
        for (Group group : groups) {
            User member = group.getMembers().get(GROUPS_DB.user.getUserId());
            if (member != null)
                result.add(group);
        }

        return result;
    }

    public static class GroupItem {
        public int admin;
        public String notice;
        public List<Integer> members;
    }
}

