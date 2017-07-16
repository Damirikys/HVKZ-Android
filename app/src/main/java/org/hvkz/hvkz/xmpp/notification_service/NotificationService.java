package org.hvkz.hvkz.xmpp.notification_service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.db.users.UsersDb;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.HashSet;
import java.util.Set;

public class NotificationService extends AbstractMessageObserver
{
    private static final NotificationService service = new NotificationService(null);

    private final int MESSAGE_NOTIFICATION_ID = 0;
    private final Set<Jid> locked;

    private NotificationService(EntityBareJid chatJid) {
        super(chatJid);
        this.locked = new HashSet<>();
    }

    private void notifyMessageReceived(ChatMessage message) throws
            XMPPException, SmackException, InterruptedException, XmppStringprepException
    {
        UsersDb.getById(message.getSenderId(), user -> {
            String contentTitle, ticker, contentText;
            Intent notificationIntent;

            contentText = (message.getBody() == null)
                    ? "новое сообщение"
                    : message.getBody();

            if (message.getChatJid().equals(message.getSenderJid())) {
                contentTitle = user.getDisplayName();
                ticker = "Новое личное сообщение";
                notificationIntent = new Intent(HVKZApp.getAppContext(), MainActivity.class);
            }
            else
            {
                contentTitle = user.getDisplayName() + " в группе";
                ticker = "Новое сообщение в группе";
                notificationIntent = new Intent(HVKZApp.getAppContext(), MainActivity.class);
            }

            notificationIntent.putExtra("buddy", message.getChatJid().toString())
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    HVKZApp.getAppContext(),
                    (int) System.currentTimeMillis(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder builder = new Notification.Builder(HVKZApp.getAppContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(ticker)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(pendingIntent)
                    .setSound(soundUri)
                    .setVibrate(new long[]{300, 300, 300, 300});

            Notification notification = builder.build();

            NotificationManager notificationManager =
                    (NotificationManager) HVKZApp.getAppContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
        });
    }

    @Override
    public void messageReceived(ChatMessage message) {
        if (message.getSenderId() == UAPIUser.getUAPIUser().getUserId() || locked.contains(message.getChatJid()))
            return;

        try { notifyMessageReceived(message); } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lock(EntityBareJid bareJid) {
        service.locked.add(bareJid);
    }

    public static void unlock(EntityBareJid bareJid) {
        service.locked.remove(bareJid);
    }

    public static NotificationService getInstance() {
        return service;
    }
}
