package org.hvkz.hvkz.modules.chats.window;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.xmpp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessagesListAdapter extends RecyclerView.Adapter<MessageViewHolder> implements Destroyable
{
    private final int LIMIT = 30;
    private int OFFSET = LIMIT * (-1);

    private final List<ChatMessage> data;
    private final MessagesSelector messagesSelector;
    private final ChatDisposer disposer;

    public MessagesListAdapter(Context context, ChatDisposer disposer) {
        this.data = new ArrayList<>();
        this.disposer = disposer;
        this.messagesSelector = new MessagesSelector(selected -> {});
        List<ChatMessage> firstPage = this.loadMore(LIMIT, OFFSET = OFFSET + LIMIT);
        if (!firstPage.isEmpty() && !firstPage.get(firstPage.size()-1).isMine(context)) {
            markAsRead();
        }

    }

    public void setOnSelectMessageListener(MessagesSelector.OnSelectMessageListener listener) {
        messagesSelector.setListener(listener);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatbubble, parent, false);

        return new MessageViewHolder(view, messagesSelector, this);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        ChatMessage currentMessage = data.get(position);
        ChatMessage beforeMessage = (position == 0)
                ? null
                : data.get(position - 1);

        holder.bindMessage(currentMessage);

        if (holder.isMine()) {
            holder.setupMineDisplay(data.get(data.size() - 1).isMine(holder.context()));
        } else {
            holder.setupNotMineDisplay(beforeMessage);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ChatMessage getItem(int position) {
        return data.get(position);
    }

    private void addAll(List<ChatMessage> messages) {
        data.addAll(0, messages);
        notifyItemRangeInserted(0, messages.size());
    }

    public List<ChatMessage> loadMore() {
        return loadMore(LIMIT, OFFSET = OFFSET + LIMIT);
    }

    private List<ChatMessage> loadMore(int limit, int offset) {
        List<ChatMessage> messages = disposer.loadMore(limit, offset);
        addAll(messages);
        return messages;
    }

    public void addMyMessage(ChatMessage message) {
        addMessage(message);
        disposer.add(message);
    }

    public void addMessage(ChatMessage message) {
        data.add(message);
        notifyItemRangeChanged(data.size() - 2, 2);
    }

    public void remove( ChatMessage message) {
        data.remove(message);
        notifyDataSetChanged();

        disposer.remove(message);
    }

    public void removeAll(List<ChatMessage> messages) {
        data.removeAll(messages);
        notifyDataSetChanged();

        disposer.removeAll(messages);
    }

    public void markAsRead() {
        for(ChatMessage m: data)
            m.setRead(true);

        notifyDataSetChanged();
        disposer.markAsRead();
    }

    @Override
    public void onDestroy() {
        messagesSelector.onDestroy();
        disposer.onDestroy();
    }
}
