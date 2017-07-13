package org.hvkz.hvkz.xmpp.message_service;

import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.xmpp.message_service.packet_listeners.AbstractMessageListener;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;
import org.hvkz.hvkz.xmpp.notification_service.NotificationService;
import org.jivesoftware.smack.AbstractXMPPConnection;

import java.util.HashSet;
import java.util.Set;

public class MessageReceiver extends AbstractMessageListener
{
    private static MessageReceiver receiver;

    private Set<AbstractMessageObserver> observers;
    private Set<MessageObserver> permanentObservers;

    private MessageReceiver(AbstractXMPPConnection connection) {
        super(connection);
        this.observers = new HashSet<>();
        this.permanentObservers = new HashSet<MessageObserver>() {{
            add(MessagesStorage.getInstance());
            add(NotificationService.getInstance());
        }};
    }

    public void subscribe(AbstractMessageObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(AbstractMessageObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void provideMessage(ChatMessage message)
    {
        for (AbstractMessageObserver observer: observers)
        {
            if (observer.getChatJid().equals(message.getChatJid())) {
                try {
                    observer.messageReceived(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (MessageObserver observer: permanentObservers)
        {
            try {
                observer.messageReceived(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void provideStatus(Status status, String chatJid, String userJid)
    {
        for (AbstractMessageObserver observer: observers)
        {
            if (!observer.getChatJid().equals(chatJid))
                continue;

            try { observer.statusReceived(status, userJid); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        for (MessageObserver observer: permanentObservers)
        {
            try {
                observer.statusReceived(status, chatJid);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static MessageReceiver instanceOf(AbstractXMPPConnection connection) {
        return receiver = new MessageReceiver(connection);
    }

    public static MessageReceiver getReceiver() {
        return receiver;
    }
}
