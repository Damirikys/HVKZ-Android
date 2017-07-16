package org.hvkz.hvkz.modules.chats.window;

import android.net.Uri;

import org.hvkz.hvkz.firebase.db.groups.GroupsDb;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class GroupChatDisposer extends ChatDisposer
{
    private Group group;
    private MultiUserChat multiUserChat;
    private ConnectionService service;

    public GroupChatDisposer(ConnectionService service, String domain) throws XmppStringprepException {
        super(JidCreate.entityBareFrom(domain + "@" + XMPPConfiguration.DOMAIN_CONFERENCE));
        this.multiUserChat = service.getMUCmanager().getMultiUserChat(chatJid);
        this.service = service;
        this.service.getMessageReceiver().subscribe(this);
        GroupsDb.getGroupByName(domain, value -> this.group = value);
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) throws SmackException.NotConnectedException, InterruptedException {
        multiUserChat.sendMessage(chatMessage
                .setChatJid(multiUserChat.getRoom())
                .buildPacket()
        );
    }

    @Override
    public void sendComposingStatus() {
        try {
            Message statusPacket = new Message();
            statusPacket.setBody(null);
            statusPacket.setType(Message.Type.groupchat);
            statusPacket.setTo(chatJid);
            statusPacket.setFrom(service.getConnection().getUser());
            ChatStateExtension extension = new ChatStateExtension(ChatState.composing);
            statusPacket.addExtension(extension);
            service.getConnection().sendStanza(statusPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendActiveStatus() {
        try {
            Message statusPacket = new Message();
            statusPacket.setBody(null);
            statusPacket.setType(Message.Type.groupchat);
            statusPacket.setTo(chatJid);
            statusPacket.setFrom(service.getConnection().getUser());
            ChatStateExtension extension = new ChatStateExtension(ChatState.active);
            statusPacket.addExtension(extension);
            service.getConnection().sendStanza(statusPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return group.getAdmin().getDisplayName();
    }

    @Override
    public String getStatus() {
        return group.getMembers().size() + " " + Tools.padezh("участник", "", "а", "ов", group.getMembers().size());
    }

    @Override
    public Uri getPhotoUri() {
        return group.getAdmin().getPhotoUrl();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.getMessageReceiver().unsubscribe(this);
        service = null;
    }
}
