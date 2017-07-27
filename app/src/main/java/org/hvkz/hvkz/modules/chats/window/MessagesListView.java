package org.hvkz.hvkz.modules.chats.window;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import org.hvkz.hvkz.adapters.EmptyItemAnimator;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.xmpp.models.ChatMessage;

import java.util.List;

public class MessagesListView extends RecyclerView
{
    private Callback<Void> keyboardCallback;
    private DateChangeListener dateChangeListener;
    private LinearLayoutManager layoutManager;
    private MessagesListOnScrollListener onScrollListener;
    private MessagesListAdapter messagesListAdapter;

    private boolean isScrolling = false;
    private boolean addMessage = false;

    public MessagesListView(Context context) {
        super(context);
    }

    public MessagesListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessagesListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMessagesAdapter(MessagesListAdapter adapter) {
        if (messagesListAdapter != null)
            messagesListAdapter.onDestroy();

        this.setAdapter(messagesListAdapter = adapter);
        this.setLayoutManager(layoutManager = new LinearLayoutManager(getContext()));
        layoutManager.scrollToPosition(
                (messagesListAdapter.getItemCount() != 0)
                        ? messagesListAdapter.getItemCount() - 1
                        : 0
        );
    }

    public void init(DateChangeListener dateChangeListener, Callback<Void> keyboardCallback) {
        this.keyboardCallback = keyboardCallback;
        this.dateChangeListener = dateChangeListener;
        this.onScrollListener = new MessagesListOnScrollListener();

        this.setItemAnimator(new EmptyItemAnimator());
        this.addOnScrollListener(onScrollListener);
    }

    public void addNewMessage(ChatMessage message) {
        post(() -> {
            messagesListAdapter.addMyMessage(message);
            if (!isScrolling) {
                addMessage = true;
                smoothScrollToPosition(messagesListAdapter.getItemCount() - 1);
            }
        });
    }

    public void addReceivedMessage(ChatMessage message) {
        post(() -> {
            messagesListAdapter.addMessage(message);
            if (!isScrolling) {
                addMessage = true;
                smoothScrollToPosition(messagesListAdapter.getItemCount() - 1);
            }

            messagesListAdapter.markAsRead();
        });
    }

    public void deleteMessages(List<ChatMessage> messages) {
        post(() -> messagesListAdapter.removeAll(messages));
    }

    public MessagesListAdapter getAdapter()
    {
        return this.messagesListAdapter;
    }

    public void scrollToBottom() {
        if (layoutManager == null) return;
        post(() -> layoutManager.scrollToPosition(messagesListAdapter.getItemCount() - 1));
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void onDestroy() {
        removeOnScrollListener(onScrollListener);
        onScrollListener.onDestroy();
        messagesListAdapter.onDestroy();
    }

    /* OnScrollListener for MessagesListView */
    private final class MessagesListOnScrollListener extends RecyclerView.OnScrollListener
    {
        private VisibilityDate visibilityDate;
        private boolean isLoading = false,
                isEnd = false;

        MessagesListOnScrollListener() {
            this.visibilityDate = new VisibilityDate();
            this.visibilityDate.start();

        }

        void onDestroy() {
            visibilityDate.interrupt();
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            int firstVisibleItems = layoutManager.findFirstVisibleItemPosition();

            if (firstVisibleItems < 0) return;

            isScrolling = lastVisibleItem != messagesListAdapter.getItemCount() - 1;

            if (isScrolling) {
                if (!addMessage) keyboardCallback.call(null);
                dateChangeListener.onDateUpdate(messagesListAdapter.getItem(firstVisibleItems));
                visibilityDate.setVisible(true);
            } else {
                addMessage = false;
            }

            if (isEnd) return;

            if (!isLoading) {
                if (firstVisibleItems == 0) {
                    isLoading = true;
                    List<ChatMessage> newPage = messagesListAdapter.loadMore();

                    if (newPage.size() == 0) {
                        isEnd = true;
                        return;
                    }

                    isLoading = false;
                }
            }
        }

        private final class VisibilityDate extends Thread
        {
            private volatile boolean visible = false;

            public void run() {
                try {
                    while (!isInterrupted()) {
                        if (visible) {
                            dateChangeListener.setDateVisibility(true);
                            visible = false;
                            sleep(1000);

                            if (!visible) {
                                dateChangeListener.setDateVisibility(false);
                            }
                        }

                        sleep(300);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    interrupt();
                }
            }

            synchronized void setVisible(boolean bool) {
                visible = bool;
            }
        }
    }
}
