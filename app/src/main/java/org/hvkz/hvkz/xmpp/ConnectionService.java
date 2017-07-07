package org.hvkz.hvkz.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;

public class ConnectionService extends Service
{
    public static final String TAG = "ConnectionService";

    private XMPPCredentials credentials;
    private AbstractXMPPConnection connection;
    private AccountManager accountManager;

    private AbstractConnectionListener connectionListener = new AbstractConnectionListener()
    {
        @Override
        public void connected(XMPPConnection xmppConnection) {
            super.connected(connection);
            connection = (AbstractXMPPConnection) xmppConnection;

            try {
                accountManager = AccountManager.getInstance(connection);
                accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.createAccount(Localpart.from(credentials.getXmppLogin()), credentials.getXmppPassword());
                connection.login();
            } catch (Exception e) {
                e.printStackTrace();

                try {
                    connection.login();
                } catch (XMPPException | SmackException | IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        @Override
        public void connectionClosed() {
            super.connectionClosed();
            Log.d(TAG, "CONNECTION CLOSED");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            super.connectionClosedOnError(e);
            new Thread(() -> {
                while (!NetworkStatus.hasConnection(ConnectionService.this)) {
                    Log.d(TAG, "NETWORK IS UNREACHABLE");
                    try { Thread.sleep(3000); }
                    catch (InterruptedException e1) { e1.printStackTrace(); }
                }

                Log.d(TAG, "NETWORK IS ACTIVE");
                tryConnect();
            }).start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        credentials = XMPPCredentials.getCredentials();
        connection = XMPPConfiguration.connectionInstance(connectionListener);
        connection.addSyncStanzaListener(packet -> System.out.println(packet.toString()), stanza -> true);
        Log.d(TAG, "SERVICE WAS CREATED!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!connection.isAuthenticated())
            new Thread(this::tryConnect).start();

        return START_STICKY;
    }

    private void tryConnect() {
        while (!connection.isConnected()) {
            Log.d(TAG, "TRY CONNECT...");
            try { connection.connect(); break; }
            catch (Exception e1) {
                e1.printStackTrace();
                Log.d(TAG, "RECONNECT FAILED");
                try { Thread.sleep(3000); }
                catch (InterruptedException e2) { e2.printStackTrace(); }
            }
        }
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<>(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            connection.disconnect(new Presence(Presence.Type.unavailable));
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
}
