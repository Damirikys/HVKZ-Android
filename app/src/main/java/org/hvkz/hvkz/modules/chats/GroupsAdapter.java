package org.hvkz.hvkz.modules.chats;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsViewHolder> implements Destroyable
{
    private HVKZApp app;
    private Handler handler;
    private List<GroupItem> groupItems;

    public GroupsAdapter(Context context, List<Group> groups) {
        this.app = ContextApp.getApp(context);
        this.handler = new Handler(Looper.getMainLooper());
        initGroups(groups);
    }

    private void initGroups(List<Group> groups) {
        this.groupItems = new ArrayList<>();

        app.bindConnectionService(service -> {
            for (Group group : groups) {
                GroupItem groupItem = new GroupItem(GroupsAdapter.this, group);
                groupItems.add(groupItem);
                service.getMessageReceiver().subscribe(groupItem);
            }
        });
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.group_layout, null);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, int position) {
        holder.hold(groupItems.get(position));
    }

    @Override
    public int getItemCount() {
        return groupItems.size();
    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDelayed(Runnable runnable, int millisec) {
        handler.postDelayed(runnable, millisec);
    }

    @Override
    public void onDestroy() {
        app.bindConnectionService(service -> {
            for (GroupItem groupItem : groupItems) {
                service.getMessageReceiver().unsubscribe(groupItem);
            }
        });
    }

    public static class GroupItem extends AbstractMessageObserver
    {
        private GroupsAdapter adapter;

        private Group group;
        private boolean isComposing;
        private int position;

        public GroupItem(GroupsAdapter adapter, Group group) {
            super(group.getGroupJid());
            this.adapter = adapter;
            this.group = group;
        }

        public ChatMessage getLastMessage() {
            return adapter.app.getMessagesStorage().getLastMessage(getChatJid());
        }

        public boolean isComposing() {
            return isComposing;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Group getGroup() {
            return group;
        }

        @Override
        public void messageReceived(ChatMessage message) throws InterruptedException {
            adapter.post(() -> {
                isComposing = false;
                adapter.notifyItemChanged(position);
            });
        }

        @Override
        public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
            if (status == ChatState.composing && !isComposing) {
                isComposing = true;
                adapter.post(() -> adapter.notifyItemChanged(position));
                adapter.postDelayed(() -> {
                    isComposing = false;
                    adapter.notifyItemChanged(position);
                }, 6000);
            }
        }
    }
}
