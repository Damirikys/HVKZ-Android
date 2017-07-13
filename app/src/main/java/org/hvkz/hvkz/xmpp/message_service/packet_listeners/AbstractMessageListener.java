package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jxmpp.jid.Jid;

public abstract class AbstractMessageListener extends AbstractPacketListener
{
    private AbstractXMPPConnection connection;
    private Jid userJid;
    private boolean onlyNotMyMessage = false;

    protected AbstractMessageListener(AbstractXMPPConnection connection)
    {
        this.connection = connection;
    }

    private Jid getUserJid() {
        if (userJid != null)
            return userJid;
        else
            return userJid = connection.getUser().asBareJid();
    }

    @Override
    public void receiveMessage(ChatMessage message)
    {
        if (onlyNotMyMessage)
        {
            if (!message.getSenderJid().equals(getUserJid()))
                provideMessage(message);
        }
        else
        {
            provideMessage(message);
        }
    }

    @Override
    public void receiveStatus(Status status, String chatJid, String userJid)
    {
        if (!userJid.equals(getUserJid()))
            StatusNotificator.sendStatus(status, chatJid, userJid);
    }

    public abstract void provideMessage(ChatMessage message);

    public abstract void provideStatus(Status status, String chatJid, String userJid);

    public void setOnlyNotMyMessage()
    {
        onlyNotMyMessage = true;
    }
}
