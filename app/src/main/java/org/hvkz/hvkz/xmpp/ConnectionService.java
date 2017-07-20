package org.hvkz.hvkz.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.adapters.SyncAdapter;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.hvkz.hvkz.xmpp.notification_service.ConnectionServiceController;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

public class ConnectionService extends Service
{
    public static final String TAG = "ConnectionService";

    private XMPPCredentials credentials;
    private AbstractXMPPConnection connection;
    private ConnectionServiceController serviceController;

    private MessageReceiver messageReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service was created!");
        connection = XMPPConfiguration.connectionInstance(serviceController =
                new ConnectionServiceController(this));

        serviceController.startLogThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (UAPIUser.isSynced()) {
            Log.d(TAG, "Already synced.");
            doAuthenticate(UAPIUser.getUAPIUser().getUserId());
        } else {
            doSync();
        }

        return START_STICKY;
    }

    private void doSync() {
        Log.d(TAG, "Try sync...");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getEmail() != null && !firebaseUser.getEmail().isEmpty()) {
            SyncInteractor.with(firebaseUser.getEmail())
                    .call(new SyncAdapter()
                    {
                        @Override
                        public void onSuccessSync(@NonNull User user) {
                            Log.d(TAG, "Success synced!");

                            UAPIUser.setCurrentUser(user);
                            doAuthenticate(user.getUserId());
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            Log.d(TAG, "Sync failed.");

                            doSync();
                        }
                    })
                    .start();
        } else {
            Log.d(TAG, "Unable to continue. Stop service.");
            stopSelf();
        }
    }

    private void doAuthenticate(int uid) {
        if (!connection.isAuthenticated()) {
            credentials = XMPPCredentials.getCredentials(uid);
            tryConnect();
        }
    }

    public void tryConnect() {
        new Thread(() -> {
            while (!connection.isConnected()) {
                Log.d(TAG, "Try connect ...");
                try { connection.connect(); break; }
                catch (Exception e) {
                    Log.d(TAG, "... failed");
                    SystemClock.sleep(2000);
                }
            }
        }).start();
    }

    public void recreateConnection() {
        connection.disconnect();
        connection = XMPPConfiguration.connectionInstance(serviceController);
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public AccountManager getAccountManager() {
        return AccountManager.getInstance(connection);
    }

    public MultiUserChatManager getMUCmanager() {
        return MultiUserChatManager.getInstanceFor(connection);
    }

    public ChatManager getChatManager() {
        return ChatManager.getInstanceFor(connection);
    }

    public MessageReceiver getMessageReceiver() {
        if (messageReceiver == null) {
            messageReceiver = MessageReceiver.instanceOf(connection);
        }

        return messageReceiver;
    }

    public XMPPCredentials getCredentials() {
        return credentials;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<>(this);
    }
}
