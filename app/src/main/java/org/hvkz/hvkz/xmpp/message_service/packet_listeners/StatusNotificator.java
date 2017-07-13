package org.hvkz.hvkz.xmpp.message_service.packet_listeners;

import android.util.Log;

import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.hvkz.hvkz.xmpp.models.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StatusNotificator
{
    private static volatile Map<String, Long> timestamps = new HashMap<>();
    private static volatile StatusWriting writingService = new StatusWriting();

    public static void sendStatus(Status status, String chatJid, String userJid) {
        Log.d("sendStatus " + status.name(), chatJid + " " + userJid);
        statusReceived(status, chatJid, userJid);
        notifyObservers(status, chatJid, userJid);
    }

    private static void statusReceived(Status status, String chat, String user) {
        String key = chat + "_" + user;

        switch (status) {
            case inactive:
                timestamps.remove(key);
                break;
            case composing:
                timestamps.put(key, System.currentTimeMillis() + 6000);
                break;
        }
    }

    private static void notifyObservers(Status status, String chat, String user) {
        Log.d("notifyObservers", status + " " + chat + " " + user);
        MessageReceiver.getReceiver().provideStatus(status, chat, user);
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
                                notifyObservers(Status.paused, params[0], params[1]);
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
