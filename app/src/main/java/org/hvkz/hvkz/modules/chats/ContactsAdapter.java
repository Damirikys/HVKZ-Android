package org.hvkz.hvkz.modules.chats;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactViewHolder> implements Destroyable
{
    private HVKZApp app;
    private Handler handler;
    private List<Contact> contacts;

    public ContactsAdapter(Context context, SparseArray<User> userSparseArray) {
        this.app = ContextApp.getApp(context);
        this.contacts = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());

        initContacts(userSparseArray);
        Collections.sort(contacts);
    }

    private void initContacts(SparseArray<User> userSparseArray) {
        app.bindConnectionService(service -> {
            for(int i = 0; i < userSparseArray.size(); i++) {
                int key = userSparseArray.keyAt(i);
                User user = userSparseArray.get(key);

                try {
                    EntityBareJid userJid = JidCreate.entityBareFrom(user.getUserId() + "@" + XMPPConfiguration.DOMAIN);
                    Contact contact = new Contact(userJid, ContactsAdapter.this);
                    contact.user = user;
                    contact.isAvailable = service.getRoster()
                            .getPresence(userJid)
                            .isAvailable();

                    service.getMessageReceiver().subscribe(contact);

                    contacts.add(contact);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        holder.hold(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
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
            for (Contact contact : contacts) {
                service.getMessageReceiver().unsubscribe(contact);
            }
        });
    }

    public static class Contact extends AbstractMessageObserver implements Comparable<Contact>
    {
        private ContactsAdapter adapter;

        private User user;
        private boolean isAvailable, isComposing;
        private int position;

        public Contact(EntityBareJid chatJid, ContactsAdapter adapter) {
            super(chatJid);
            this.adapter = adapter;
        }

        public User getUser() {
            return user;
        }

        public ChatMessage getLastMessage() {
            return adapter.app.getMessagesStorage().getLastMessage(getChatJid());
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public boolean isComposing() {
            return isComposing;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void messageReceived(ChatMessage message) throws InterruptedException {
            adapter.post(() -> {
                isComposing = false;
                Collections.sort(adapter.contacts);
                adapter.notifyDataSetChanged();
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

        @Override
        public void presenceReceived(Presence.Type type) {
            this.isAvailable = (type == Presence.Type.available);
            adapter.post(() -> {
                isComposing = false;
                adapter.notifyItemChanged(position);
            });
        }

        @Override
        public int compareTo(@NonNull Contact o) {
            ChatMessage first = this.getLastMessage();
            ChatMessage second = o.getLastMessage();

            if (first.getTimestamp() > second.getTimestamp())
                return -1;
            if (first.getTimestamp() < second.getTimestamp())
                return 1;

            return 0;
        }
    }
}
