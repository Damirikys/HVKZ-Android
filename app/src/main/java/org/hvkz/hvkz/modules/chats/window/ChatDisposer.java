package org.hvkz.hvkz.modules.chats.window;

import android.content.Context;
import android.net.Uri;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.modules.chats.window.disposers.GroupChatDisposer;
import org.hvkz.hvkz.modules.chats.window.disposers.PersonalChatDisposer;
import org.hvkz.hvkz.modules.chats.window.ui.ChatWindowPresenter;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.messaging.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public abstract class ChatDisposer extends AbstractMessageObserver implements Destroyable
{
    private final ConnectionService service;

    private final EntityBareJid chatJid;
    private List<AbstractMessageObserver> observers;

    @Inject
    MessagesStorage storage;

    public ChatDisposer(ConnectionService service, EntityBareJid jid) {
        super(jid);
        ContextApp.getApp(service).component().inject(this);
        this.service = service;
        this.chatJid = jid;
        this.observers = new ArrayList<>();

        EventChannel.connect(this);
        getService().getMessageReceiver().subscribe(this);
    }

    public final void sendMessage(ChatMessage message) throws SmackException.NotConnectedException, InterruptedException {
        if (getService().getConnection().isAuthenticated()) {
            getService().getConnection().sendStanza(message.setChatJid(chatJid).buildPacket(getMessageType()));
        } else {
            throw new SmackException.NotConnectedException();
        }
    }

    private void sendStatusPacket(ChatState state) {
        try {
            Message statusPacket = new Message();
            statusPacket.setBody(null);
            statusPacket.setType(getMessageType());
            statusPacket.setTo(chatJid);
            statusPacket.setFrom(getService().getConnection().getUser());
            ChatStateExtension extension = new ChatStateExtension(state);
            statusPacket.addExtension(extension);
            getService().getConnection().sendStanza(statusPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendComposingStatus() {
        sendStatusPacket(ChatState.composing);
    }

    public void sendActiveStatus() {
        sendStatusPacket(ChatState.active);
    }

    public void sendInactiveStatus() {
        sendStatusPacket(ChatState.inactive);
    }

    public void onToolbarClick(Context context) {}

    public abstract Message.Type getMessageType();

    public abstract String getTitle();

    public abstract String getDefaultStatus();

    public abstract String getActiveStatus();

    public abstract Uri getPhotoUri();

    public ConnectionService getService() {
        return service;
    }

    @Override
    public EntityBareJid getChatJid() {
        return chatJid;
    }

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

    @Override
    public void presenceReceived(Presence.Type type) {
        for (AbstractMessageObserver observer : observers) {
            observer.presenceReceived(type);
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

    public void remove(ChatMessage message) {
        storage.deleteMessage(message);
    }

    @Override
    public void onDestroy() {
        EventChannel.disconnect(this);
        getService().getMessageReceiver().unsubscribe(this);
        observers.clear();
    }

    public static void obtain(Callback<ChatDisposer> disposerCallback,
                              ChatWindowPresenter presenter,
                              ChatType chatType,
                              String domain) {
        HVKZApp app = ContextApp.getApp(presenter.context());
        app.bindConnectionService(service -> {
            switch (chatType) {
                case MULTI_USER_CHAT:
                    app.getGroupsStorage().getGroupByName(domain, group ->
                            disposerCallback.call(new GroupChatDisposer(presenter, service, group)));
                    break;
                case PERSONAL_CHAT:
                    app.getUsersStorage().getByIdFromCache(Integer.valueOf(domain), user -> {
                        try {
                            disposerCallback.call(new PersonalChatDisposer(service, user));
                        } catch (XmppStringprepException e) {
                            disposerCallback.call(null);
                        }
                    });
                    break;
            }
        });
    }
}
