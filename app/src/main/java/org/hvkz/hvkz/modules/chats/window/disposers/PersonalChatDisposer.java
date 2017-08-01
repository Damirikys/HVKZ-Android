package org.hvkz.hvkz.modules.chats.window.disposers;

import android.net.Uri;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.utils.JidFactory;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.stringprep.XmppStringprepException;

public class PersonalChatDisposer extends ChatDisposer
{
    private User buddy;

    public PersonalChatDisposer(ConnectionService service, User buddy) throws XmppStringprepException {
        super(service, JidFactory.from(buddy));
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
                .getPresence(getChatJid().asBareJid())
                .isAvailable();

        return (isAvailable)
                ? getService().getString(R.string.online)
                : getService().getString(R.string.offline);
    }

    @Override
    public String getActiveStatus() {
        return getService().getString(R.string.in_chat);
    }

    @Override
    public Uri getPhotoUri() {
        return buddy.getPhotoUrl();
    }
}
