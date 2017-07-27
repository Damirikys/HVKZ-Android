package org.hvkz.hvkz.modules.chats.window;

import android.net.Uri;

import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class PersonalChatDisposer extends ChatDisposer
{
    private User buddy;

    public PersonalChatDisposer(ConnectionService service, User buddy) throws XmppStringprepException {
        super(service, JidCreate.entityBareFrom(buddy.getUserId() + "@" + XMPPConfiguration.DOMAIN));
        this.buddy = buddy;
    }

    @Override
    public Message.Type getMessageType() {
        return Message.Type.chat;
    }

    @Override
    public String getTitle() {
        return buddy.getDisplayName();
    }

    @Override
    public String getDefaultStatus() {
        boolean isAvailable = getService()
                .getRoster()
                .getPresence(chatJid.asBareJid())
                .isAvailable();

        return (isAvailable) ? "в сети" : "не в сети";
    }

    @Override
    public String getActiveStatus() {
        return "в диалоге";
    }

    @Override
    public Uri getPhotoUri() {
        return buddy.getPhotoUrl();
    }
}
