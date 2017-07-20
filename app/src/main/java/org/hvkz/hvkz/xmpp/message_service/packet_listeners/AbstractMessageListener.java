package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

public abstract class AbstractMessageListener extends AbstractPacketListener
{
    private BareJid userJid;
    private XMPPConnection connection;

    protected AbstractMessageListener(XMPPConnection connection) {
        this.connection = connection;
    }

    private BareJid getUserJid() {
        if (userJid != null) return userJid;
        else return userJid = connection.getUser().asBareJid();
    }

    @Override
    public void receiveMessage(ChatMessage message) {
        if (!message.getSenderJid().equals(getUserJid()))
            provideMessage(message);
    }

    @Override
    public void receiveStatus(ChatState status, EntityBareJid chatJid, BareJid userJid) {
        if (!userJid.equals(getUserJid())) {
            provideStatus(status, chatJid, userJid);
        }
    }

    public abstract void provideMessage(ChatMessage message);

    public abstract void provideStatus(ChatState status, EntityBareJid chatJid, BareJid userJid);
}
