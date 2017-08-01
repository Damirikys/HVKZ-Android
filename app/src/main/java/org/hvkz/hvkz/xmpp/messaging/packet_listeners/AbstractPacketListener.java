package org.hvkz.hvkz.xmpp.messaging.packet_listeners;

import org.hvkz.hvkz.xmpp.config.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

abstract class AbstractPacketListener extends AbstractRosterStatusListener
        implements MessageListener, StanzaListener, IncomingChatMessageListener
{
    public static final String TAG = "AbstractPacketListener";

    @Override
    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
        ChatStateExtension extension = (ChatStateExtension) packet.getExtension(ChatStateExtension.NAMESPACE);
        ChatState state = extension.getChatState();

        try {
            String[] from = packet.getFrom().asUnescapedString().split("/");
            if (state != null && from[0].contains("conference"))
                receiveStatus(state,
                        JidCreate.entityBareFrom(from[0]),
                        JidCreate.entityBareFrom(from[1] + "@" + XMPPConfiguration.DOMAIN)
                );
            else receiveStatus(state, JidCreate.entityBareFrom(from[0]), JidCreate.entityBareFrom(from[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processMessage(Message message) {
        if (message.getBody() == null) return;
        receiveMessage(ChatMessage.createFrom(message).setChatJid(message.getFrom()));
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        if (message.getBody() == null) return;
        receiveMessage(ChatMessage.createFrom(message).setChatJid(from));
    }

    public abstract void receiveMessage(ChatMessage message);

    public abstract void receiveStatus(ChatState status, EntityBareJid chatJid, EntityBareJid userJid);
}
