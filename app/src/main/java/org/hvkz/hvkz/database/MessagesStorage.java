package org.hvkz.hvkz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.notification_service.NotificationService;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class MessagesStorage extends AbstractMessageObserver
{
    private static final String TAG = "MessagesStorage";
    private static MessagesStorage instance;

    @Inject
    User user;

    private SQLiteDatabase database;
    private MessagesDbHelper messagesDbHelper;

    private MessagesStorage(Context c) {
        super(null);
        HVKZApp.component().inject(this);
        this.messagesDbHelper = new MessagesDbHelper(c);
        this.database = messagesDbHelper.getWritableDatabase();
        try { messagesDbHelper.onCreate(database); }
        catch (SQLiteException ignored){}
    }

    @Override
    public void messageReceived(ChatMessage message) {
        if (!messageExist(message)) {
            writeMessage(message);
            NotificationService.getInstance().messageReceived(message);
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

        if (cursor.moveToFirst()) {
            int jsonIndex = cursor.getColumnIndex(MessagesDbHelper.JSON);
            int readIndex = cursor.getColumnIndex(MessagesDbHelper.READ_MARK);

            ChatMessage message = JSONFactory.fromJson(cursor.getString(jsonIndex), ChatMessage.class);
            message.setRead(cursor.getString(readIndex).equals("1"));

            cursor.close();
            return message;
        } else {
            cursor.close();
            return null;
        }
    }

    public boolean messageExist(ChatMessage message) {
        String query = "Select * from " + MessagesDbHelper.TABLE_NAME + " where " +
                MessagesDbHelper.JSON + " = '" + JSONFactory.toJson(message) + "'";

        Cursor cursor = database.rawQuery(query, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public List<ChatMessage> getMessages(BareJid chat, int limit, int offset) {
        return extractMessages(chat.getLocalpartOrThrow().intern(), limit, offset);
    }

    public List<ChatMessage> getMessages(String chat, int limit, int offset) {
        return extractMessages(chat, limit, offset);
    }

    private List<ChatMessage> extractMessages(String chat, int limit, int offset) {
        List<ChatMessage> messages = new ArrayList<>();

        String selection = MessagesDbHelper.CHAT + " == '"+ chat +"' AND "
                + MessagesDbHelper.UID + " == " + user.getUserId();

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
        Log.d(TAG, "writeMessage isRead " + message.isRead());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesDbHelper.UID, user.getUserId());
        contentValues.put(MessagesDbHelper.CHAT, message.getChatJid().getLocalpart().intern());
        contentValues.put(MessagesDbHelper.JSON, JSONFactory.toJson(message));
        contentValues.put(MessagesDbHelper.STANZA_ID, message.getStanzaId());
        contentValues.put(MessagesDbHelper.READ_MARK, message.isRead());

        database.insert(MessagesDbHelper.TABLE_NAME, null, contentValues);
    }

    public void deleteMessages(Collection<ChatMessage> messageCollection) {
        database.beginTransaction();
        for(ChatMessage message: messageCollection) {
            database.delete(MessagesDbHelper.TABLE_NAME, MessagesDbHelper.STANZA_ID + " = '" + message.getStanzaId() + "'", null);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void clearHistory(BareJid jid) {
        database.delete(MessagesDbHelper.TABLE_NAME,
                MessagesDbHelper.CHAT + " = '" + jid.getLocalpartOrThrow().intern() + "'", null);
    }

    public void markAsRead(EntityBareJid chatJid) {
        markAsRead(chatJid.getLocalpart().intern());
    }

    public void markAsRead(String chat) {
        Log.d(TAG, "markAsRead " + chat);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesDbHelper.READ_MARK, true);

        database.update(MessagesDbHelper.TABLE_NAME, contentValues,
                MessagesDbHelper.READ_MARK + " == 0 AND " + MessagesDbHelper.CHAT + " == '" + chat + "'", null);
    }

    public static MessagesStorage getInstance() {
        if (instance == null) instance = new MessagesStorage(HVKZApp.getAppContext());
        return instance;
    }
}
