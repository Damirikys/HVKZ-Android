package org.hvkz.hvkz.xmpp.utils;

import android.util.Log;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.annotations.EventReceiver;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MUCConnectionManager
{
    private static final String TAG = "MUCConnectionManager";

    private HVKZApp app;

    public MUCConnectionManager(HVKZApp app) {
        this.app = app;
    }

    @EventReceiver
    public void onGroupStateChanged(List<Group> groups) {
        app.getOptionsStorage().isAccepted(value -> {
            if (value) {
                mucHandlerProcess(groups);
            }
        });
    }

    private void mucHandlerProcess(List<Group> groups) {
        app.bindConnectionService(service -> {
            MultiUserChatManager manager = service.getMUCmanager();
            Map<String, Group> groupMap = new HashMap<>();
            for (Group group : groups) {
                groupMap.put(group.getGroupName(), group);
            }

            for (EntityBareJid bareJid : manager.getJoinedRooms()) {
                if (!groupMap.containsKey(bareJid.getLocalpart().toString())) {
                    leave(service, manager.getMultiUserChat(bareJid));
                }
            }

            for (Group group : groups) {
                MultiUserChat multiUserChat = manager.getMultiUserChat(group.getGroupJid());
                if (!multiUserChat.isJoined()) {
                    join(service, multiUserChat);
                }
            }
        });
    }

    private void join(ConnectionService service, MultiUserChat multiUserChat) {
        new Thread(() -> {
            Log.d(TAG, "Join in new group " + multiUserChat.toString());
            try {
                multiUserChat.addMessageListener(service.getMessageReceiver());
                ChatMessage lastMessage = app.getMessagesStorage().getLastMessage(multiUserChat.getRoom());
                MucEnterConfiguration.Builder configBuilder = multiUserChat.getEnterConfigurationBuilder(
                        Resourcepart.from(service.getCredentials().getXmppLogin())
                );

                if (lastMessage == null) {
                    configBuilder.requestHistorySince(new Date(1469779914));
                } else {
                    configBuilder.requestHistorySince(new Date(lastMessage.getTimestamp() * 1000));
                }

                multiUserChat.join(configBuilder.build());
            } catch (Exception e) {
                Log.d(TAG, "Can't join to MUC because " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void leave(ConnectionService service, MultiUserChat chat) {
        new Thread(() -> {
            Log.d(TAG, "Leave from group " + chat.toString());
            try {
                chat.removeMessageListener(service.getMessageReceiver());
                chat.leave();
            } catch (Exception ignored) {}
        }).start();
    }
}
