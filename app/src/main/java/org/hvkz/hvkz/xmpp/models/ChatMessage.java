package org.hvkz.hvkz.xmpp.models;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;

public class ChatMessage
{
    private static final Gson gson = new GsonBuilder().create();

    private String stanza_id;

    private int sender;
    private int recipient;
    private String chatJid;
    private String message;
    private String[] imageAttachments;
    private ForwardedMessage[] forwardedMessages;
    private long datetime;
    private boolean isRead = true;

    public ChatMessage(String message) {
        this.message = message;
        this.sender = UAPIUser.getUAPIUser().getUserId();
        this.datetime = System.currentTimeMillis();
    }

    public ChatMessage() {
        this.sender = UAPIUser.getUAPIUser().getUserId();
        this.datetime = System.currentTimeMillis();
    }

    public String getStanzaId() {
        return String.valueOf(datetime);
    }

    public ChatMessage setStanzaId(String id) {
        this.stanza_id = id;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ChatMessage setMessage(String _message) {
        message = _message;
        return this;
    }

    public int getSender() {
        return sender;
    }

    public Jid getSenderJid() {
        try {
            return JidCreate.bareFrom(sender + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setSender(int id) {
        sender = id;
        return this;
    }

    public int getRecipient() {
        return recipient;
    }

    public Jid getRecipientJid() {
        try {
            return JidCreate.bareFrom(recipient + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setRecipient(int id) {
        recipient = id;
        chatJid = (chatJid == null) ? String.valueOf(recipient) + "@" + XMPPConfiguration.DOMAIN : chatJid;
        return this;
    }

    public long getDatetime() {
        return datetime;
    }

    public ChatMessage setDatetime(long _datetime) {
        datetime = _datetime;
        return this;
    }

    public String[] getImageAttachments() {
        return imageAttachments;
    }

    public Jid getChatJid() {
        try {
            return JidCreate.from(chatJid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ChatMessage setChatJid(Jid jid) {
        chatJid = jid.toString();
        return this;
    }

    public ChatMessage setImageAttachments(String... attachments) {
        imageAttachments = attachments;
        return this;
    }

    public ChatMessage setForwardedMessages(List<ForwardedMessage> messages) {
        this.forwardedMessages = messages.toArray(new ForwardedMessage[messages.size()]);
        return this;
    }

    public ForwardedMessage[] getForwardedMessages() {
        return forwardedMessages;
    }

    public ChatMessage setRead(boolean bool) {
        isRead = bool;
        return this;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isMine() {
        return UAPIUser.getUAPIUser().getUserId() == sender;
    }

    public Message buildPacket() throws XmppStringprepException {
        String jsonFormat = gson.toJson(this);

        Message packet = new Message(JidCreate.domainBareFrom(recipient + "@" + XMPPConfiguration.DOMAIN));
        packet.setBody(jsonFormat);

        return packet;
    }

    public static ChatMessage createFrom(@NonNull Message message) throws IllegalArgumentException
    {
        DelayInformation delay;
        delay = (DelayInformation) message.getExtension("urn:xmpp:delay");

        if (message.getBody() == null)
            throw new IllegalArgumentException("Message body is empty.");

        ChatMessage chatMessage = gson.fromJson(message.getBody(), ChatMessage.class);
        chatMessage.setStanzaId(message.getStanzaId());

        if (delay != null)
            chatMessage.setDatetime(delay.getStamp().getTime());

        return chatMessage;
    }

    public ForwardedMessage toForward() {
        return new ForwardedMessage()
                .setSender(sender)
                .setMessage(message)
                .setImageAttachments(imageAttachments)
                .setDatetime(datetime);
    }

    public static class ForwardedMessage
    {
        int sender;
        String message;
        String[] imageAttachments;
        long datetime;

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

        ForwardedMessage setImageAttachments(String... images) {
            this.imageAttachments = images;
            return this;
        }

        public String[] getImageAttachments() {
            return imageAttachments;
        }

        ForwardedMessage setDatetime(long _datetime) {
            this.datetime = _datetime;
            return this;
        }

        public long getDatetime() {
            return datetime;
        }
    }

    @Override
    public int hashCode() {
        return (int) datetime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) obj;
            return (this.datetime == message.datetime) & (this.message.equals(message.message));
        } else {
            return false;
        }
    }
}
