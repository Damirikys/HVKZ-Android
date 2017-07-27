package org.hvkz.hvkz.xmpp.message_service;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

public abstract class AbstractMessageObserver implements MessageObserver
{
    private EntityBareJid chatJid;

    public AbstractMessageObserver(EntityBareJid chatJid) {
        this.chatJid = chatJid;
    }

    @Override
    public void messageReceived(ChatMessage message) throws InterruptedException {
        // Stub
    }

    @Override
    public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
        // Stub
    }

    @Override
    public void presenceReceived(Presence.Type type) {
        // Stub
    }

    public EntityBareJid getChatJid() {
        return chatJid;
    }
}
