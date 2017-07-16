package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import android.util.Log;

import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StatusNotificator
{
    private static volatile Map<String, Long> timestamps = new HashMap<>();
    private static volatile StatusWriting writingService = new StatusWriting();

    public static void sendStatus(ChatState status, EntityBareJid chatJid, BareJid userJid) {
        Log.d("sendStatus " + status.name(), chatJid + " " + userJid);
        statusReceived(status, chatJid, userJid);
        notifyObservers(status, chatJid, userJid);
    }

    private static void statusReceived(ChatState status, EntityBareJid chat, BareJid user) {
        String key = chat.asEntityBareJidString() + "_" + user.asUnescapedString();

        switch (status) {
            case inactive:
                timestamps.remove(key);
                break;
            case composing:
                timestamps.put(key, System.currentTimeMillis() + 6000);
                break;
        }
    }

    private static void notifyObservers(ChatState status, EntityBareJid chat, BareJid user) {
        Log.d("notifyObservers", status + " " + chat.asEntityBareJidString() + " " + user.asUnescapedString());
        //MessageReceiver.getReceiver().provideStatus(status, chat, user);
    }

    private static class StatusWriting extends Thread
    {
        StatusWriting() {
            start();
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run()
        {
            while (true)
            {
                synchronized (this) {
                    Set<String> keySet = new HashSet<>(timestamps.keySet());
                    if (keySet.size() != 0) {
                        for (String s : keySet) {
                            if (timestamps.get(s) < System.currentTimeMillis()) {
                                timestamps.remove(s);
                                String[] params = s.split("_");
                                //notifyObservers(ChatState.paused, params[0], params[1]);
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
