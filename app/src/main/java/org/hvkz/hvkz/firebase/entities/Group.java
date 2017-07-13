package org.hvkz.hvkz.firebase.entities;

import android.util.SparseArray;

import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;

public class Group
{
    private String groupName;

    private User admin;
    private SparseArray<User> members;
    private String notice;

    public Group setGroupName(String name) {
        this.groupName = name;
        return this;
    }

    public Group setMembers(SparseArray<User> members) {
        this.members = members;
        return this;
    }

    public Group setAdmin(User admin) {
        this.admin = admin;
        return this;
    }

    public Group setNotice(String notice) {
        this.notice = notice;
        return this;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupAddress() {
        return groupName + "@" + XMPPConfiguration.DOMAIN_CONFERENCE;
    }

    public SparseArray<User> getMembers() {
        return members;
    }

    public User getAdmin() {
        return admin;
    }

    public String getNotice() {
        return notice;
    }
}
