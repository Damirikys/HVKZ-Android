package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import android.util.Log;

import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;
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
import org.jxmpp.stringprep.XmppStringprepException;

abstract class AbstractPacketListener implements MessageListener, StanzaListener, IncomingChatMessageListener
{
    @Override
    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
        Log.d("PacketListener", packet.toXML().toString());
        ChatStateExtension extension = (ChatStateExtension) packet.getExtension(ChatStateExtension.NAMESPACE);
        ChatState state = extension.getChatState();
        Status status = null;

        switch (state) {
            case active:
                status = Status.active;
                break;
            case inactive:
                status = Status.inactive;
                break;
            case composing:
                status = Status.composing;
                break;
            case paused:
                status = Status.paused;
                break;
        }

        String[] from = packet.getFrom().toString().split("/");
        if (status != null && from[0].contains("conference"))
            receiveStatus(status, from[0], from[1] + "@" + XMPPConfiguration.DOMAIN);
        else
            receiveStatus(status, from[0], from[0]);
    }

    @Override
    public void processMessage(Message message) {
        if (message.getBody() == null) return;
        String[] jidFrom = message.getFrom().asUnescapedString().split("/");
        if (jidFrom.length == 2)
        {
            String chatJid = jidFrom[0];
            processMessage(chatJid, message);
        }
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        processMessage(
                from.asEntityBareJidString(),
                message
        );
    }

    public abstract void receiveMessage(ChatMessage message);

    public abstract void receiveStatus(Status status, String chatJid, String userJid);

    private void processMessage(String chatJid, Message message)
    {
        if (message.getBody() != null) {
            ChatMessage chatMessage = ChatMessage.createFrom(message);
            try {
                chatMessage.setChatJid(JidCreate.from(chatJid));
                receiveMessage(chatMessage);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
    }

}
