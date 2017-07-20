package org.hvkz.hvkz.modules.chats.window;

import android.net.Uri;

import org.hvkz.hvkz.firebase.db.users.UsersDb;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class PersonalChatDisposer extends ChatDisposer
{
    private User buddy;
    private ConnectionService service;
    private Chat chat;

    public PersonalChatDisposer(ConnectionService service, String domain) throws XmppStringprepException {
        super(JidCreate.entityBareFrom(domain + "@" + XMPPConfiguration.DOMAIN));
        this.service = service;
        this.chat = service.getChatManager().chatWith(chatJid);
        this.service.getMessageReceiver().subscribe(this);
        UsersDb.getById(Integer.valueOf(domain), value -> this.buddy = value);
    }

    @Override
    public void sendMessage(ChatMessage message) throws SmackException.NotConnectedException, InterruptedException {
        chat.send(message
                .setChatJid(chatJid)
                .buildPacket()
        );
    }

    @Override
    public void sendComposingStatus() {
        try {
            Message statusPacket = new Message();
            statusPacket.setBody(null);
            statusPacket.setType(Message.Type.chat);
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
            statusPacket.setType(Message.Type.chat);
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
        return buddy.getDisplayName();
    }

    @Override
    public String getStatus() {
        return Roster.getInstanceFor(service.getConnection()).getPresence(chatJid).getStatus();
    }

    @Override
    public Uri getPhotoUri() {
        return buddy.getPhotoUrl();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service.getMessageReceiver().unsubscribe(this);
        service = null;
    }
}
