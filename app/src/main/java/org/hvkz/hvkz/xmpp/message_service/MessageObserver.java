package org.hvkz.hvkz.xmpp.message_service;

import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;

public interface MessageObserver
{
    void messageReceived(ChatMessage message) throws InterruptedException;

    void statusReceived(Status status, String userJid) throws InterruptedException;
}
