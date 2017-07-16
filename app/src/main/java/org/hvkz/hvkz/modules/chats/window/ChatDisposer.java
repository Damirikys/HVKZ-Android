package org.hvkz.hvkz.modules.chats.window;

import android.net.Uri;

import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.notification_service.NotificationService;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

public abstract class ChatDisposer extends AbstractMessageObserver implements Destroyable
{
    protected final EntityBareJid chatJid;
    protected final MessagesStorage storage;
    private List<AbstractMessageObserver> observers;

    public ChatDisposer(EntityBareJid bareJid) {
        super(bareJid);
        this.chatJid = bareJid;
        this.storage = MessagesStorage.getInstance();
        this.observers = new ArrayList<>();
        NotificationService.lock(chatJid);
    }

    public abstract void sendMessage(ChatMessage message) throws SmackException.NotConnectedException, InterruptedException;

    public abstract void sendComposingStatus();

    public abstract void sendActiveStatus();

    public abstract String getTitle();

    public abstract String getStatus();

    public abstract Uri getPhotoUri();

    @Override
    public void messageReceived(ChatMessage message) throws InterruptedException {
        for (AbstractMessageObserver observer : observers) {
            observer.messageReceived(message);
        }
    }

    @Override
    public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
        for (AbstractMessageObserver observer : observers) {
            observer.statusReceived(status, userJid);
        }
    }

    public void attachMessageObserver(AbstractMessageObserver observer) {
        observers.add(observer);
    }

    public List<ChatMessage> loadMore(int limit, int offset) {
        return storage.getMessages(chatJid, limit, offset);
    }

    public void add(ChatMessage message) {
        storage.writeMessage(message);
    }

    public void markAsRead() {
        storage.markAsRead(chatJid);
    }

    public void removeAll(List<ChatMessage> messages) {
        storage.deleteMessages(messages);
    }

    @Override
    public void onDestroy() {
        observers.clear();
        observers = null;
        NotificationService.unlock(chatJid);
    }

    public static ChatDisposer obtain(ConnectionService service, ChatType chatType, String domain)
            throws XmppStringprepException {
        switch (chatType) {
            case MULTI_USER_CHAT:
                return new GroupChatDisposer(service, domain);
            default:
                return null;
        }
    }
}
