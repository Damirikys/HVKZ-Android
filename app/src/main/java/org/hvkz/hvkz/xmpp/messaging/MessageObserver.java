package org.hvkz.hvkz.xmpp.messaging;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;

public interface MessageObserver
{
    void messageReceived(ChatMessage message) throws InterruptedException;

    void statusReceived(ChatState status, BareJid userJid) throws InterruptedException;

    void presenceReceived(Presence.Type type);
}
