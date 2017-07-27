package org.hvkz.hvkz.xmpp.notification_service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashSet;
import java.util.Set;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;
import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class NotificationService extends AbstractMessageObserver
{
    private final HVKZApp app;
    private final int MESSAGE_NOTIFICATION_ID = 0;
    private final Set<String> locked;
    private final int LARGE_ICON_SIZE;

    public NotificationService(HVKZApp app) {
        super(null);
        this.app = app;
        this.locked = new HashSet<>();
        this.LARGE_ICON_SIZE = dpToPx(app.getResources().getDisplayMetrics(), 32);
    }

    private void notifyMessageReceived(ChatMessage message) throws
            XMPPException, SmackException, InterruptedException, XmppStringprepException
    {
        app.getUsersStorage().getByIdFromCache(message.getSenderId(), user -> {
            String contentTitle, ticker, contentText;
            Intent notificationIntent;
            Bundle routeBundle = new Bundle();

            contentText = (message.getBody() == null)
                    ? "новое сообщение"
                    : message.getBody();

            if (message.getChatJid().equals(message.getSenderJid())) {
                contentTitle = user.getShortName();
                ticker = "Новое личное сообщение";
                notificationIntent = new Intent(app, MainActivity.class);
                routeBundle.putSerializable(CHAT_TYPE_KEY, ChatType.PERSONAL_CHAT);
            } else {
                contentTitle = user.getShortName() + " в группе";
                ticker = "Новое сообщение в группе";
                notificationIntent = new Intent(app, MainActivity.class);
                routeBundle.putSerializable(CHAT_TYPE_KEY, ChatType.MULTI_USER_CHAT);
            }

            routeBundle.putString(DOMAIN_KEY, message.getChatJid().getLocalpart().toString());
            notificationIntent.putExtra("route", routeBundle)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(app)
                    .setSmallIcon(R.drawable.ic_message_white)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setTicker(ticker)
                    .setWhen(message.getTimestamp())
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(PendingIntent.getActivity(
                            app, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT
                    )).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{300, 300, 300, 300});

            try {
                Bitmap bitmap = Glide.with(app)
                        .load(user.getPhotoUrl())
                        .asBitmap()
                        .into(LARGE_ICON_SIZE, LARGE_ICON_SIZE)
                        .get();

                builder.setLargeIcon(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Notification notification = builder.build();

            NotificationManager notificationManager =
                    (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
        });
    }

    @Override
    public void messageReceived(ChatMessage message) {
        if (message.getSenderId() == app.getCurrentUser().getUserId() ||
                locked.contains(message.getChatJid().getLocalpart().toString())) {
            return;
        }

        try { notifyMessageReceived(message); } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lock(String localpart) {
        locked.add(localpart);
    }

    public void unlock(String localpart) {
        locked.remove(localpart);
    }

    public boolean isLocked(String localpart) {
        return locked.contains(localpart);
    }
}
