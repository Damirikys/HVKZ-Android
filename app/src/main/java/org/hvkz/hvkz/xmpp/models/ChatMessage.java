package org.hvkz.hvkz.xmpp.models;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage
{
    private transient Jid chatJid;
    private transient boolean isRead;
    private transient Forwarded forwardedThis;

    private int senderId;
    private int recipientId;
    private String body;
    private List<String> images;
    private List<Forwarded> forwarded;
    private long timestamp;

    public ChatMessage(Context context, String body) {
        this.body = body;
        this.senderId = ContextApp.getApp(context).getCurrentUser().getUserId();
    }

    public ChatMessage(Context context) {
        this.senderId = ContextApp.getApp(context).getCurrentUser().getUserId();
    }

    public String getStanzaId() {
        return String.valueOf(timestamp);
    }

    public String getBody() {
        return body;
    }

    public ChatMessage setBody(String _message) {
        body = _message;
        return this;
    }

    public int getSenderId() {
        return senderId;
    }

    public EntityBareJid getSenderJid() {
        try {
            return JidCreate.entityBareFrom(senderId + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setSenderId(int id) {
        senderId = id;
        return this;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public BareJid getRecipientJid() {
        try {
            return JidCreate.bareFrom(recipientId + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setRecipientId(int id) {
        recipientId = id;
        try {
            chatJid = (chatJid == null) ? JidCreate.bareFrom(String.valueOf(recipientId) + "@" + XMPPConfiguration.DOMAIN) : chatJid;
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ChatMessage setTimestamp(long _datetime) {
        timestamp = _datetime;
        return this;
    }

    public List<String> getImages() {
        return images;
    }

    public EntityBareJid getChatJid() {
        try {
            return JidCreate.entityBareFrom(chatJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setChatJid(Jid jid) {
        chatJid = jid;
        return this;
    }

    public ChatMessage setImages(List<Uri> attachments) {
        images = new ArrayList<>();
        for (Uri uri : attachments)
            images.add(uri.toString());
        return this;
    }

    public ChatMessage setForwarded(List<Forwarded> messages) {
        this.forwarded = new ArrayList<>(messages);
        return this;
    }

    public List<Forwarded> getForwarded() {
        return forwarded;
    }

    public ChatMessage setRead(boolean bool) {
        isRead = bool;
        return this;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isMine(Context context) {
        return ContextApp.getApp(context).getCurrentUser().getUserId() == senderId;
    }

    public Message buildPacket(Message.Type type) {
        String jsonFormat = JSONFactory.toJson(this.setTimestamp(System.currentTimeMillis()));

        Message packet = new Message();
        packet.setBody(jsonFormat);
        packet.setTo(chatJid);
        packet.setType(type);

        return packet;
    }

    public static ChatMessage createFrom(@NonNull Message message) {
        return JSONFactory.fromJson(message.getBody(), ChatMessage.class)
                .setChatJid(message.getFrom().asBareJid());
    }

    public Forwarded toForward() {
        if (forwardedThis != null)
            return forwardedThis;
        else return forwardedThis = new Forwarded()
                .setSender(senderId)
                .setMessage(body)
                .setImages(images)
                .setTimestamp(timestamp);
    }

    public static class Forwarded
    {
        int sender;
        String message;
        List<String> images;
        long timestamp;

        Forwarded setSender(int id) {
            this.sender = id;
            return this;
        }

        public int getSender() {
            return sender;
        }

        Forwarded setMessage(String _message) {
            this.message = _message;
            return this;
        }

        public String getMessage() {
            return message;
        }

        Forwarded setImages(List<String> images) {
            this.images = images;
            return this;
        }

        public List<String> getImages() {
            return images;
        }

        Forwarded setTimestamp(long _datetime) {
            this.timestamp = _datetime;
            return this;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    @Override
    public int hashCode() {
        return (int) timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) obj;
            return (this.timestamp == message.timestamp) & (this.body.equals(message.body));
        } else {
            return false;
        }
    }
}
