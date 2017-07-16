package org.hvkz.hvkz.modules.chats;

import org.hvkz.hvkz.xmpp.models.ChatMessage;

public interface DateChangeListener
{
    void onDateUpdate(ChatMessage message);
    void setDateVisibility(boolean bool);
}