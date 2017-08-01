package org.hvkz.hvkz.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.utils.controllers.NotificationController;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.messaging.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MessagesStorage extends AbstractMessageObserver
{
    private static final String TAG = "MessagesStorage";

    private HVKZApp app;
    private NotificationController notificationController;
    private MessagesDbHelper helper;
    private SQLiteDatabase database;

    public MessagesStorage(HVKZApp app) {
        super(null);
        this.app = app;
        this.notificationController = app.getNotificationService();
        this.helper = new MessagesDbHelper(app);
        this.database = helper.getWritableDatabase();

        try { helper.onCreate(database); }
        catch (SQLiteException ignored){}
    }

    @Override
    public void messageReceived(ChatMessage message) {
        if (!messageExist(message)) {
            writeMessage(message);
            notificationController.messageReceived(message);
        }
    }

    @Override
    public void statusReceived(ChatState status, BareJid userJid) {
        if (status == ChatState.active) markAsRead(userJid.getLocalpartOrThrow().intern());
    }

    public ChatMessage getLastMessage(BareJid jid) {
        String chat = jid.getLocalpartOrThrow().intern();
        String selection = MessagesDbHelper.CHAT + " == '"+ chat +"'";
        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + MessagesDbHelper.TABLE_NAME +
                        " WHERE " + selection +
                        " ORDER BY " + MessagesDbHelper.ID + " DESC", null
        );

        ChatMessage message = null;
        if (cursor.moveToFirst()) {
            int jsonIndex = cursor.getColumnIndex(MessagesDbHelper.JSON);
            int readIndex = cursor.getColumnIndex(MessagesDbHelper.READ_MARK);

            message = JSONFactory.fromJson(cursor.getString(jsonIndex), ChatMessage.class);
            message.setRead(cursor.getString(readIndex).equals("1"));
        }

        cursor.close();
        return message;
    }

    public boolean messageExist(ChatMessage message) {
        String selection = MessagesDbHelper.STANZA_ID + " == '" + message.getStanzaId() + "' AND "
                + MessagesDbHelper.CHAT + " == '" + message.getChatJid().getLocalpart().intern() + "'";

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + MessagesDbHelper.TABLE_NAME + " WHERE " + selection, null);

        boolean result = cursor.moveToFirst();
        cursor.close();

        return result;
    }

    public List<ChatMessage> getMessages(Jid chat, int limit, int offset) {
        return extractMessages(chat.getLocalpartOrThrow().intern(), limit, offset);
    }

    private List<ChatMessage> extractMessages(String chat, int limit, int offset) {
        List<ChatMessage> messages = new ArrayList<>();

        String selection = MessagesDbHelper.CHAT + " == '"+ chat +"' AND "
                + MessagesDbHelper.UID + " == " + app.getCurrentUser().getUserId();

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + MessagesDbHelper.TABLE_NAME +
                        " WHERE " + selection +
                        " ORDER BY " + MessagesDbHelper.ID + " DESC" +
                        " LIMIT "+ offset +","+ limit, null
        );

        if (cursor.moveToFirst()) {
            int jsonIndex = cursor.getColumnIndex(MessagesDbHelper.JSON);
            int readIndex = cursor.getColumnIndex(MessagesDbHelper.READ_MARK);

            do {
                ChatMessage message = JSONFactory.fromJson(cursor.getString(jsonIndex), ChatMessage.class);
                message.setRead(cursor.getString(readIndex).equals("1"));
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        Collections.reverse(messages);

        return messages;
    }

    public void writeMessage(ChatMessage message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesDbHelper.UID, app.getCurrentUser().getUserId());
        contentValues.put(MessagesDbHelper.CHAT, message.getChatJid().getLocalpart().intern());
        contentValues.put(MessagesDbHelper.JSON, JSONFactory.toJson(message));
        contentValues.put(MessagesDbHelper.STANZA_ID, message.getStanzaId());
        contentValues.put(MessagesDbHelper.READ_MARK, message.isRead());

        database.insert(MessagesDbHelper.TABLE_NAME, null, contentValues);
    }

    public void deleteMessages(Collection<ChatMessage> messageCollection) {
        database.beginTransaction();
        for(ChatMessage message: messageCollection) {
            deleteMessage(message);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void deleteMessage(ChatMessage message) {
        database.delete(MessagesDbHelper.TABLE_NAME, MessagesDbHelper.STANZA_ID + " = '" + message.getStanzaId() + "'", null);
    }

    public void clearHistory(BareJid jid) {
        clearHistory(jid.getLocalpartOrThrow().intern());
    }

    public void clearHistory(String localpart) {
        database.delete(MessagesDbHelper.TABLE_NAME,
                MessagesDbHelper.CHAT + " = '" + localpart + "'", null);
    }

    public void markAsRead(Jid chatJid) {
        markAsRead(chatJid.getLocalpartOrThrow().intern());
    }

    public void markAsRead(String chat) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesDbHelper.READ_MARK, true);

        database.update(MessagesDbHelper.TABLE_NAME, contentValues,
                MessagesDbHelper.READ_MARK + " == 0 AND " + MessagesDbHelper.CHAT + " == '" + chat + "'", null);
    }

    public List<String> getExistedChats() {
        String query = "Select distinct " + MessagesDbHelper.CHAT + " from " + MessagesDbHelper.TABLE_NAME;

        Cursor cursor = database.rawQuery(query, null);
        List<String> collection = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int chatIndex = cursor.getColumnIndex(MessagesDbHelper.CHAT);
            do {
                collection.add(cursor.getString(chatIndex));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return collection;
    }
}
