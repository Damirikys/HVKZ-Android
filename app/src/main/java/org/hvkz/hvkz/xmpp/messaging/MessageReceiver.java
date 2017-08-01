package org.hvkz.hvkz.xmpp.messaging;

import android.content.Context;
import android.util.Log;

import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.messaging.packet_listeners.AbstractMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;

import java.util.HashSet;
import java.util.Set;

public class MessageReceiver extends AbstractMessageListener
{
    private Set<AbstractMessageObserver> observers;
    private Set<MessageObserver> permanentObservers;

    private MessageReceiver(Context context, EntityFullJid userJid) {
        super(userJid);
        this.observers = new HashSet<>();
        this.permanentObservers = new HashSet<MessageObserver>() {{
            add(ContextApp.getApp(context).getMessagesStorage());
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
        for (MessageObserver observer: permanentObservers) {
            try {
                observer.messageReceived(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (AbstractMessageObserver observer: observers) {
            if (observer.getChatJid().equals(message.getChatJid())) {
                try {
                    observer.messageReceived(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void provideStatus(ChatState status, EntityBareJid chatJid, EntityBareJid userJid) {
        for (AbstractMessageObserver observer: observers) {
            if (!observer.getChatJid().equals(chatJid))
                continue;

            try { observer.statusReceived(status, userJid); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        EntityBareJid bareJid = presence.getFrom().asEntityBareJidIfPossible();
        for (AbstractMessageObserver observer : observers) {
            if (observer.getChatJid().equals(bareJid)) {
                observer.presenceReceived(presence.getType());
            }
        }
    }

    public static MessageReceiver instanceOf(Context context, EntityFullJid userJid) {
        return new MessageReceiver(context, userJid);
    }
}
