package org.hvkz.hvkz.firebase.entities;

import android.util.SparseArray;

import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

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

    public EntityBareJid getGroupJid() {
        try {
            return JidCreate.entityBareFrom(getGroupAddress());
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
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
