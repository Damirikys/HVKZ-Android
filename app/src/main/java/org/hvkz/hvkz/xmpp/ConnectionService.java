package org.hvkz.hvkz.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.groups.GroupsDb;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;

public class ConnectionService extends Service
{
    public static final String TAG = "ConnectionService";

    @Inject
    User user;

    private XMPPCredentials credentials;
    private AbstractXMPPConnection connection;
    private AccountManager accountManager;

    private MessageReceiver messageReceiver;
    private MultiUserChatManager MUCmanager;
    private ChatManager chatManager;

    private AbstractConnectionListener connectionListener = new AbstractConnectionListener()
    {
        @Override
        public void connected(XMPPConnection xmppConnection) {
            super.connected(connection);
            connection = (AbstractXMPPConnection) xmppConnection;
            MUCmanager = MultiUserChatManager.getInstanceFor(connection);
            chatManager = ChatManager.getInstanceFor(connection);
            messageReceiver = MessageReceiver.instanceOf(connection);

            connection.addSyncStanzaListener(messageReceiver, stanza ->
                    stanza.getExtension(ChatStateExtension.NAMESPACE) != null);
            chatManager.addIncomingListener(messageReceiver);

            try {

                accountManager = AccountManager.getInstance(connection);
                accountManager.sensitiveOperationOverInsecureConnection(true);
                accountManager.createAccount(Localpart.from(credentials.getXmppLogin()), credentials.getXmppPassword());
                connection.login();
            } catch (Exception e) {
                try {
                    connection.login();
                } catch (XMPPException | SmackException | IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            super.authenticated(connection, resumed);
            GroupsDb.getMyGroups(groups -> {
                for (Group group : groups) {
                    try {
                        MultiUserChat multiUserChat = MUCmanager.getMultiUserChat(JidCreate.entityBareFrom(
                                Localpart.from(group.getGroupName()),
                                Domainpart.from(XMPPConfiguration.DOMAIN_CONFERENCE)
                        ));

                        multiUserChat.addMessageListener(messageReceiver);
                        MucEnterConfiguration config = multiUserChat
                                .getEnterConfigurationBuilder(Resourcepart.from(String.valueOf(user.getUserId())))
                                .requestHistorySince(new Date(MessagesStorage.getInstance()
                                        .getLastMessage(multiUserChat.getRoom())
                                        .getTimestamp()))
                                .build();

                        multiUserChat.join(config);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
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
        HVKZApp.component().inject(this);
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

    public MultiUserChatManager getMUCmanager() {
        return MUCmanager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
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
