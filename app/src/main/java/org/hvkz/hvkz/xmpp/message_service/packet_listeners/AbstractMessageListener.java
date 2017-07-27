package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;

public abstract class AbstractMessageListener extends AbstractPacketListener
{
    private EntityFullJid userJid;

    protected AbstractMessageListener(EntityFullJid userJid) {
        this.userJid = userJid;
    }

    private EntityBareJid getUserJid() {
        return userJid.asEntityBareJid();
    }

    @Override
    public void receiveMessage(ChatMessage message) {
        if (!message.getSenderJid().equals(getUserJid())) {
            provideMessage(message);
        }
    }

    @Override
    public void receiveStatus(ChatState status, EntityBareJid chatJid, EntityBareJid userJid) {
        if (!userJid.equals(getUserJid())) {
            provideStatus(status, chatJid, userJid);
        }
    }

    public abstract void provideMessage(ChatMessage message);

    public abstract void provideStatus(ChatState status, EntityBareJid chatJid, EntityBareJid userJid);
}
