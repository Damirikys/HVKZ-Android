package org.hvkz.hvkz.modules.chats.window;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.View;

import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;

public class GroupChatDisposer extends ChatDisposer
{
    private Group group;

    public GroupChatDisposer(ConnectionService service, Group group) {
        super(service, group.getGroupJid());
        this.group = group;
    }

    @Override
    public void sendInactiveStatus() {
        // Not send inactive status in MUC
    }

    @Override
    public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
        if (status == ChatState.composing) {
            super.statusReceived(status, userJid);
        }
    }

    @Override
    public void presenceReceived(Presence.Type type) {
        // Not send presence
    }

    @Override
    public Message.Type getMessageType() {
        return Message.Type.groupchat;
    }

    @Override
    public String getTitle() {
        return group.getAdmin().getDisplayName();
    }

    @Override
    public String getDefaultStatus() {
        return group.getMembers().size() + " " + Tools.padezh("участник", "", "а", "ов", group.getMembers().size());
    }

    @Override
    public String getActiveStatus() {
        return getDefaultStatus();
    }

    @Override
    public Uri getPhotoUri() {
        return group.getAdmin().getPhotoUrl();
    }

    @Override
    public void onToolbarClick(Context context) {
        AlertDialog membersListDialog = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle("Список участников")
                .create();

        View view = MembersListWrapper.inflate(membersListDialog, Tools.asList(group.getMembers()));
        membersListDialog.setView(view);
        membersListDialog.show();
    }
}
