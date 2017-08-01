package org.hvkz.hvkz.modules.chats.window.disposers;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.EventReceiver;
import org.hvkz.hvkz.event.Event;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.modules.chats.window.members.MembersListWrapper;
import org.hvkz.hvkz.modules.chats.window.ui.ChatWindowPresenter;
import org.hvkz.hvkz.modules.moderate.GroupEditorFragment;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;

import java.util.List;

public class GroupChatDisposer extends ChatDisposer
{
    private Group group;
    private ChatWindowPresenter presenter;

    public GroupChatDisposer(ChatWindowPresenter presenter, ConnectionService service, Group group) {
        super(service, group.getGroupJid());
        this.group = group;
        this.presenter = presenter;
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
        return group.getMembers().size() + " " + Tools.declension("участник", "", "а", "ов", group.getMembers().size());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(R.string.members_roster);

        ContextApp.getApp(context).getOptionsStorage().isModerated(can -> {
            if (can) {
                builder.setNeutralButton(R.string.edit_group, (dialog, which) -> {
                    GroupEditorFragment dialogFrag = GroupEditorFragment.newInstance(group);
                    dialogFrag.setParentFab(new FloatingActionButton(context));
                    dialogFrag.show(
                            presenter.getViewHandler().window(Fragment.class).getFragmentManager(),
                            dialogFrag.getTag()
                    );
                });
            }

            AlertDialog dialog = builder.create();

            View view = MembersListWrapper.inflate(dialog, group);
            dialog.setView(view);
            dialog.show();
        });
    }

    @EventReceiver
    public void onGroupModify(Event<List<Group>> event) {
        if (event.getType() == Event.EventType.GROUPS_DATA_WAS_CHANGED) {
            List<Group> data = event.getData();
            for (Group groupData : data) {
                if (groupData.getGroupName().equals(group.getGroupName())) {
                    group = groupData;
                    EventChannel.send(new Event<>(Event.EventType.UPDATE_GROUP_CHAT_WINDOW));
                    break;
                }
            }
        }
    }
}
