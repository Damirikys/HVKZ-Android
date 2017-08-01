package org.hvkz.hvkz.modules.chats.window.ui;

import org.hvkz.hvkz.xmpp.messaging.ChatMessage;

public interface OnDateChangeListener
{
    void onDateUpdate(ChatMessage message);
    void setDateVisibility(boolean bool);
}