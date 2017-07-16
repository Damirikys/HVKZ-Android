package org.hvkz.hvkz.xmpp.models;

import android.support.annotation.NonNull;

import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;

public class ChatMessage
{
    private transient String chatJid;
    private transient boolean isRead;

    private int senderId;
    private int recipientId;
    private String body;
    private List<String> images;
    private List<ForwardedMessage> forwarded;
    private long timestamp;

    public ChatMessage(String body) {
        this.body = body;
        this.senderId = UAPIUser.getUAPIUser().getUserId();
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage() {
        this.senderId = UAPIUser.getUAPIUser().getUserId();
        this.timestamp = System.currentTimeMillis();
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

    public BareJid getSenderJid() {
        try {
            return JidCreate.bareFrom(senderId + "@" + XMPPConfiguration.DOMAIN);
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
        chatJid = (chatJid == null) ? String.valueOf(recipientId) + "@" + XMPPConfiguration.DOMAIN : chatJid;
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
        chatJid = jid.toString();
        return this;
    }

    public ChatMessage setImages(List<String> attachments) {
        images = attachments;
        return this;
    }

    public ChatMessage setForwarded(List<ForwardedMessage> messages) {
        this.forwarded = messages;
        return this;
    }

    public List<ForwardedMessage> getForwarded() {
        return forwarded;
    }

    public ChatMessage setRead(boolean bool) {
        isRead = bool;
        return this;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isMine() {
        return UAPIUser.getUAPIUser().getUserId() == senderId;
    }

    public Message buildPacket() {
        String jsonFormat = JSONFactory.toJson(this);

        Message packet = null;
        try {
            packet = new Message(chatJid, jsonFormat);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        return packet;
    }

    public static ChatMessage createFrom(@NonNull Message message) {
        DelayInformation delay = (DelayInformation) message.getExtension("urn:xmpp:delay");
        ChatMessage chatMessage = JSONFactory.fromJson(message.getBody(), ChatMessage.class)
                .setChatJid(message.getFrom().asEntityBareJidOrThrow());

        if (delay != null)
            chatMessage.setTimestamp(delay.getStamp().getTime());

        return chatMessage;
    }

    public ForwardedMessage toForward() {
        return new ForwardedMessage()
                .setSender(senderId)
                .setMessage(body)
                .setImages(images)
                .setTimestamp(timestamp);
    }

    public static class ForwardedMessage
    {
        int sender;
        String message;
        List<String> images;
        long timestamp;

        ForwardedMessage setSender(int id) {
            this.sender = id;
            return this;
        }

        public int getSender() {
            return sender;
        }

        ForwardedMessage setMessage(String _message) {
            this.message = _message;
            return this;
        }

        public String getMessage() {
            return message;
        }

        ForwardedMessage setImages(List<String> images) {
            this.images = images;
            return this;
        }

        public List<String> getImages() {
            return images;
        }

        ForwardedMessage setTimestamp(long _datetime) {
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
