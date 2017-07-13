package org.hvkz.hvkz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.hvkz.hvkz.xmpp.models.Status;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MessagesStorage extends AbstractMessageObserver
{
    private static final String TAG = "MessagesStorage";

    private SQLiteDatabase database;
    private MessagesDbHelper messagesDbHelper;
    private static MessagesStorage instance;
    private static Gson gson = new GsonBuilder().create();

    private MessagesStorage(Context c)
    {
        super("");
        this.messagesDbHelper = new MessagesDbHelper(c);
        this.database = messagesDbHelper.getWritableDatabase();
    }

    @Override
    public void messageReceived(ChatMessage message) {
        writeMessage(message);
    }

    @Override
    public void statusReceived(Status status, String userJid) {
        if (status == Status.active) markAsRead(userJid);
    }

    public ChatMessage getLastMessage(String _jid)
    {
        String jid = _jid.split("@")[0];
        String selection = MessagesDbHelper.CHAT + " == '"+ jid +"'";

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + MessagesDbHelper.TABLE_NAME +
                        " WHERE " + selection +
                        " ORDER BY " + MessagesDbHelper.ID + " DESC", null
        );

        if (cursor.moveToFirst())
        {
            int jsonIndex = cursor.getColumnIndex(MessagesDbHelper.JSON);
            int readIndex = cursor.getColumnIndex(MessagesDbHelper.ISREAD);

            ChatMessage message = gson.fromJson(cursor.getString(jsonIndex), ChatMessage.class);
            message.setRead(cursor.getString(readIndex).equals("1"));

            cursor.close();
            return message;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    public List<ChatMessage> getMessages(String _jid, int limit, int offset)
    {
        String jid = _jid.split("@")[0];
        List<ChatMessage> messages = new ArrayList<>();

        String selection = MessagesDbHelper.CHAT + " == '"+ jid +"'";

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + MessagesDbHelper.TABLE_NAME +
                        " WHERE " + selection +
                        " ORDER BY " + MessagesDbHelper.ID + " DESC" +
                        " LIMIT "+ offset +","+ limit, null
        );

        if (cursor.moveToFirst())
        {
            int jsonIndex = cursor.getColumnIndex(MessagesDbHelper.JSON);
            int readIndex = cursor.getColumnIndex(MessagesDbHelper.ISREAD);

            do {
                ChatMessage message = gson.fromJson(cursor.getString(jsonIndex), ChatMessage.class);
                message.setRead(cursor.getString(readIndex).equals("1"));
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        Collections.reverse(messages);

        return messages;
    }

    public void writeMessage(ChatMessage message)
    {
        try {
            Log.d(TAG, "writeMessage: " + message.getMessage());
            String chatJid = message.getChatJid().toString().split("@")[0];

            ContentValues contentValues = new ContentValues();
            contentValues.put(MessagesDbHelper.CHAT, chatJid);
            contentValues.put(MessagesDbHelper.STANZA_ID, message.getStanzaId());
            contentValues.put(MessagesDbHelper.JSON, message.buildPacket().getBody());
            contentValues.put(MessagesDbHelper.ISREAD, message.isRead());

            database.insert(MessagesDbHelper.TABLE_NAME, null, contentValues);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessages(Collection<ChatMessage> messageCollection)
    {
        for(ChatMessage message: messageCollection)
            database.delete(MessagesDbHelper.TABLE_NAME, MessagesDbHelper.STANZA_ID + " = '" + message.getStanzaId() + "'", null);
    }

    public void clearHistory(String jid)
    {
        Log.d("clearHistory", jid);
        database.delete(MessagesDbHelper.TABLE_NAME, MessagesDbHelper.CHAT + " = '" + jid.split("@")[0] + "'", null);
    }

    public void markAsRead(String jid)
    {
        Log.d(TAG, "markAsRead");
        String buddy = jid.split("@")[0];
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesDbHelper.ISREAD, true);

        database.update(MessagesDbHelper.TABLE_NAME, contentValues, MessagesDbHelper.ISREAD + " == 0 AND " + MessagesDbHelper.CHAT + " == '" + buddy + "'", null);
    }

    public static MessagesStorage getInstance() {
        if (instance == null) instance = new MessagesStorage(HVKZApp.getAppContext());
        return instance;
    }
}
