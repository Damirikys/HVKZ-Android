package org.hvkz.hvkz.xmpp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.StubActivity;
import org.hvkz.hvkz.adapters.SyncAdapter;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.hvkz.hvkz.xmpp.notification_service.ConnectionServiceController;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.List;

import static org.hvkz.hvkz.xmpp.BootBroadcast.BROADCAST_KEY;

public class ConnectionService extends Service
{
    public static final String TAG = "ConnectionService";

    private XMPPCredentials credentials;
    private AbstractXMPPConnection connection;
    private ConnectionServiceController serviceController;
    private MessageReceiver messageReceiver;
    private LogThread logThread;

    public void startLogThread() {
        Log.d(TAG, "LogThread started!");
        if (logThread != null) {
            logThread.interrupt();
        }

        logThread = new LogThread();
        logThread.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service was created!");
        User user = ContextApp.getApp(this).getCurrentUser();
        if (user != null) {
            credentials = XMPPCredentials.getCredentials(user.getUserId());
        }

        connection = XMPPConfiguration.connectionInstance(serviceController =
                new ConnectionServiceController(this));

        startLogThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra(BROADCAST_KEY, false)) {
            Log.d(TAG, "Internet is availabe! (From broadcast receiver)");
            tryConnect();
        } else {
            if (ContextApp.getApp(this).isSynced()) {
                Log.d(TAG, "Already synced.");
                doAuthenticate(ContextApp.getApp(this).getCurrentUser().getUserId());
            } else {
                doSync();
            }
        }

        return START_STICKY;
    }

    private void doSync() {
        Log.d(TAG, "Try sync...");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getEmail() != null && !firebaseUser.getEmail().isEmpty()) {
            SyncInteractor.with(this, firebaseUser.getEmail())
                    .call(new SyncAdapter() {
                        @Override
                        public void onSuccessSync(@NonNull User user) {
                            Log.d(TAG, "Success synced!");
                            ContextApp.getApp(ConnectionService.this).setCurrentUser(user);
                            doAuthenticate(user.getUserId());
                        }

                        @Override
                        public void onFailed(Throwable throwable) {
                            Log.d(TAG, "Sync failed.");
                        }
                    })
                    .start();
        } else {
            Log.d(TAG, "Unable to continue. Stop service.");
            stopSelf();
        }
    }

    private void doAuthenticate(int uid) {
        credentials = XMPPCredentials.getCredentials(uid);
        tryConnect();
    }

    public boolean connectIsPossible() {
        return NetworkStatus.hasConnection(this) &&
                !connection.isConnected() &&
                !connection.isAuthenticated() &&
                FirebaseAuth.getInstance().getCurrentUser() != null &&
                ContextApp.getApp(this).isSynced();
    }

    public void tryConnect() {
        if (connectIsPossible()) {
            new Thread(() -> {
                try {
                    Log.d(TAG, "Try connect ...");
                    connection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "... connect failed");
                }
            }).start();
        } else {
            Log.d(TAG, "Ooops... we can't do connect =(");
            Log.d(TAG, "connected " + connection.isConnected());
            Log.d(TAG, "aouthenticated " + connection.isAuthenticated());
        }
    }

    public void recreateConnection() {
        connection = XMPPConfiguration.connectionInstance(serviceController);
    }

    public void reanimate() {
        Log.d(TAG, "Start StubActivity and reanimate service!");

        recreateConnection();
        Intent intent = new Intent(this, StubActivity.class);

        if (isAppOnForeground()) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        startActivity(intent);
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public Roster getRoster() {
        return Roster.getInstanceFor(connection);
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
            try {
                messageReceiver = MessageReceiver.instanceOf(
                        this, JidCreate.entityFullFrom(credentials.getXmppLogin() + "@" + XMPPConfiguration.DOMAIN + "/Android")
                );
            } catch (XmppStringprepException e) {
                messageReceiver = MessageReceiver.instanceOf(
                        this, connection.getUser()
                );
            }
        }

        return messageReceiver;
    }

    public XMPPCredentials getCredentials() {
        return credentials;
    }

    private boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<>(this);
    }

    private class LogThread extends Thread
    {
        @Override
        public void run() {
            super.run();

            while (!isInterrupted()) {
                Log.d(TAG, getName() + " : Has connection: " + NetworkStatus.hasConnection(ConnectionService.this));
                Log.d(TAG, System.currentTimeMillis() + " : " +
                        "authenticated " + connection.isAuthenticated() + " | " +
                        "connected " + connection.isConnected()
                );

                if (messageReceiver != null) {
                    if (!connection.isAuthenticated() || !NetworkStatus.hasConnection(ConnectionService.this)) {
                        reanimate();
                    }
                }
//
//                if (!NetworkStatus.hasConnection(ConnectionService.this)) {
//                    if (connection.isAuthenticated()) {
//                        Log.d(TAG, "Start reanimate because we lost connection but stay online.");
//                        reanimate();
//                    } else {
//                        tryConnect();
//                    }
//                } else {
//                    if (!connection.isAuthenticated()) {
//                        tryConnect();
//                    }
//                }

                SystemClock.sleep(60000);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (logThread != null) logThread.interrupt();
        connection.disconnect();
    }
}
