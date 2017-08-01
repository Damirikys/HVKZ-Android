package org.hvkz.hvkz.modules.chats.chatsroster;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.EventReceiver;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.messaging.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;

import java.util.ArrayList;
import java.util.List;

import xyz.santeri.wvp.WrappingViewPager;

public class GroupsPagerAdapter extends PagerAdapter implements Destroyable
{
    private Handler handler;

    private HVKZApp app;
    private ViewPager viewPager;
    private ViewPagerIndicator indicator;
    private List<GroupItem> groupItems;

    private int mCurrentPosition = -1;

    public GroupsPagerAdapter(ViewPager viewPager, ViewPagerIndicator indicator, List<Group> groups) {
        this.app = ContextApp.getApp(viewPager.getContext());
        this.viewPager = viewPager;
        this.indicator = indicator;
        this.groupItems = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        onGroupsUpdate(groups);
        EventChannel.connect(this);
    }

    @EventReceiver
    public void onGroupsUpdate(List<Group> groups) {
        unsubscribe(service -> {
            mCurrentPosition = -1;
            groupItems.clear();

            for (Group group : groups) {
                GroupItem groupItem = new GroupItem(GroupsPagerAdapter.this, group);
                groupItems.add(groupItem);
                service.getMessageReceiver().subscribe(groupItem);
            }

            handler.post(() -> {
                viewPager.setAdapter(GroupsPagerAdapter.this);
                indicator.setupWithViewPager(viewPager);
            });
        });
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view = LayoutInflater.from(collection.getContext()).inflate(R.layout.group_layout, collection, false);
        GroupViewHolder groupViewHolder = new GroupViewHolder(view);
        GroupsPagerAdapter.GroupItem groupItem = groupItems.get(position);
        groupItem.bindHolder(groupViewHolder);
        collection.addView(groupViewHolder.getView());
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return groupItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return groupItems.get(position).getGroup().getGroupName();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            View view = (View) object;
            WrappingViewPager pager = (WrappingViewPager) container;
            if (view != null) {
                mCurrentPosition = position;
                pager.onPageChanged(view);
            }
        }
    }

    public void post(Runnable runnable) {
        indicator.post(runnable);
    }

    public void postDelayed(Runnable runnable, int millisec) {
        viewPager.postDelayed(runnable, millisec);
    }

    private void unsubscribe(Callback<ConnectionService> serviceCallback) {
        app.bindConnectionService(service -> {
            for (GroupItem groupItem : groupItems)
                service.getMessageReceiver().unsubscribe(groupItem);
            serviceCallback.call(service);
        });
    }

    @Override
    public void onDestroy() {
        unsubscribe((service) -> EventChannel.disconnect(this));
    }

    public static class GroupItem extends AbstractMessageObserver
    {
        private GroupsPagerAdapter adapter;
        private GroupViewHolder holder;

        private Group group;
        private boolean isComposing;

        public GroupItem(GroupsPagerAdapter adapter, Group group) {
            super(group.getGroupJid());
            this.adapter = adapter;
            this.group = group;
        }

        public void bindHolder(GroupViewHolder holder) {
            this.holder = holder;
            hold();
        }

        @Override
        public void messageReceived(ChatMessage message) throws InterruptedException {
            isComposing = false;
            adapter.post(this::hold);
        }

        @Override
        public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
            if (status == ChatState.composing && !isComposing) {
                isComposing = true;
                adapter.post(this::hold);

                adapter.postDelayed(() -> {
                    isComposing = false;
                    hold();
                }, 6000);
            }
        }

        private void hold() {
            if (holder != null)
                holder.bind(this);
        }

        public ChatMessage getLastMessage() {
            return adapter.app.getMessagesStorage().getLastMessage(getChatJid());
        }

        public boolean isComposing() {
            return isComposing;
        }

        public Group getGroup() {
            return group;
        }
    }
}