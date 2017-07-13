package org.hvkz.hvkz.xmpp.models;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class ChatImpl<T>
{
    private T chat;

    public ChatImpl(T type) {
        this.chat = type;
    }

    public T getChat() {
        return chat;
    }

    public void sendMessage(Message message) throws SmackException.NotConnectedException, InterruptedException {
        ChatType type = ChatType.valueOf(chat.getClass().getSimpleName());
        type.send(chat, message);
    }

    private enum ChatType {
        Chat, MultiUserChat;

        public void send(Object chat, Message message) throws SmackException.NotConnectedException, InterruptedException {
            switch (this) {
                case Chat:
                    ((Chat) chat).send(message);
                    break;
                case MultiUserChat:
                    ((MultiUserChat) chat).sendMessage(message);
                    break;
            }
        }
    }
}
