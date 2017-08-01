package org.hvkz.hvkz.modules.chats.window.tools;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public final class MessagesSelector implements Destroyable
{
    private List<ChatMessage.Forwarded> selectedMessages;
    private OnSelectMessageListener listener;
    private boolean selectedMode;

    public MessagesSelector(OnSelectMessageListener listener) {
        this.selectedMessages = new ArrayList<>();
        this.listener = listener;
        this.selectedMode = false;
    }

    public List<ChatMessage.Forwarded> getSelectedMessages() {
        return selectedMessages;
    }

    public boolean isEnable() {
        return selectedMode;
    }

    public void setListener(OnSelectMessageListener listener) {
        this.listener = listener;
    }

    public void setSelectorEnable(boolean bool) {
        selectedMode = bool;
    }

    public void onMessageClick(ChatMessage message, Callback<Boolean> onCompleteCallback) {
        if (isEnable() && !message.getBody().isEmpty()) {
            ChatMessage.Forwarded fm = message.toForward();
            if (!selectedMessages.contains(fm)) {
                selectedMessages.add(fm);
                onCompleteCallback.call(true);
            } else {
                selectedMessages.remove(fm);
                onCompleteCallback.call(false);
            }
        }

        if (selectedMessages.size() == 0)
            setSelectorEnable(false);

        listener.onSelect(selectedMessages);
    }

    @Override
    public void onDestroy() {
        listener = null;
    }

    public interface OnSelectMessageListener {
        void onSelect(List<ChatMessage.Forwarded> selected);
    }
}
