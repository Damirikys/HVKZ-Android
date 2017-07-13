package org.hvkz.hvkz.xmpp.message_service;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;

public abstract class AbstractMessageObserver implements MessageObserver
{
    private String chatJid;

    public AbstractMessageObserver(String chatJid) {
        this.chatJid = chatJid;
    }

    @Override
    public void messageReceived(ChatMessage message) throws InterruptedException {
        throw new InterruptedException("Don't have messageReceived implementation.");
    }

    @Override
    public void statusReceived(Status status, String userJid) throws InterruptedException {
        throw new InterruptedException("Don't have statusReceived implementation.");
    }

    String getChatJid() {
        return chatJid;
    }
}
