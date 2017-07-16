package org.hvkz.hvkz.xmpp.message_service;

import android.util.Log;

import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.xmpp.message_service.packet_listeners.AbstractMessageListener;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashSet;
import java.util.Set;

public class MessageReceiver extends AbstractMessageListener
{
    private Set<AbstractMessageObserver> observers;
    private Set<MessageObserver> permanentObservers;

    private MessageReceiver(AbstractXMPPConnection connection) {
        super(connection);
        this.observers = new HashSet<>();
        this.permanentObservers = new HashSet<MessageObserver>() {{
            add(MessagesStorage.getInstance());
        }};
    }

    public void subscribe(AbstractMessageObserver observer) {
        Log.d(TAG, "subscriber #" + observers.size());
        observers.add(observer);
    }

    public void unsubscribe(AbstractMessageObserver observer) {
        Log.d(TAG, "unsubscribe #" + observers.size());
        observers.remove(observer);
    }

    @Override
    public void provideMessage(ChatMessage message) {
        for (AbstractMessageObserver observer: observers) {
            if (observer.getChatJid().equals(message.getChatJid())) {
                try {
                    observer.messageReceived(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (MessageObserver observer: permanentObservers) {
            try {
                observer.messageReceived(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void provideStatus(ChatState status, EntityBareJid chatJid, BareJid userJid) {
        for (AbstractMessageObserver observer: observers) {
            if (!observer.getChatJid().equals(chatJid))
                continue;

            try { observer.statusReceived(status, userJid); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public static MessageReceiver instanceOf(AbstractXMPPConnection connection) {
        return new MessageReceiver(connection);
    }
}
