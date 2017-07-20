package org.hvkz.hvkz.xmpp.notification_service;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.StubActivity;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.groups.GroupsDb;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.xmpp.AbstractConnectionListener;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
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

public class ConnectionServiceController extends AbstractConnectionListener
{
    private static final String TAG = "ConnectionController";
    private ConnectionService service;
    private LogThread logThread;

    public ConnectionServiceController(ConnectionService service) {
        this.service = service;
        this.logThread = new LogThread();
    }

    public void startLogThread() {
        logThread.start();
    }

    @Override
    public void connected(XMPPConnection connection) {
        super.connected(connection);
        connection.addSyncStanzaListener(packet ->
                System.out.println(packet.toString()), stanza -> true);

        ChatManager chatManager = service.getChatManager();
        MessageReceiver messageReceiver = service.getMessageReceiver();

        chatManager.addIncomingListener(messageReceiver);
        connection.addSyncStanzaListener(messageReceiver, stanza ->
                stanza.getExtension(ChatStateExtension.NAMESPACE) != null);

        String xmppLogin = service.getCredentials().getXmppLogin();
        String xmppPassword = service.getCredentials().getXmppPassword();

        try {
            AccountManager accountManager = service.getAccountManager();
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(xmppLogin), xmppPassword);

            service.getConnection().login(xmppLogin, xmppPassword);
        } catch (Exception e) {
            try {
                service.getConnection().login(xmppLogin, xmppPassword);
            } catch (XMPPException | SmackException | IOException | InterruptedException e1) {
                Log.d(TAG, "Login failed. Caused by " + e1.getMessage());
            }
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        super.authenticated(connection, resumed);

        GroupsDb.getMyGroups(groups -> {
            MultiUserChatManager MUCmanager = service.getMUCmanager();
            for (Group group : groups) {
                try {
                    MultiUserChat multiUserChat = MUCmanager.getMultiUserChat(JidCreate.entityBareFrom(
                            Localpart.from(group.getGroupName()),
                            Domainpart.from(XMPPConfiguration.DOMAIN_CONFERENCE)
                    ));

                    multiUserChat.addMessageListener(service.getMessageReceiver());
                    ChatMessage lastMessage = MessagesStorage.getInstance()
                            .getLastMessage(multiUserChat.getRoom());

                    MucEnterConfiguration.Builder configBuilder = multiUserChat
                            .getEnterConfigurationBuilder(Resourcepart.from(service.getCredentials().getXmppLogin()));

                    if (lastMessage == null) {
                        multiUserChat.join(configBuilder.build());
                    } else {
                        MucEnterConfiguration config = configBuilder
                                .requestHistorySince(new Date(lastMessage.getTimestamp()))
                                .build();

                        multiUserChat.join(config);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Can't join to MUC because " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void connectionClosed() {
        super.connectionClosed();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        super.connectionClosedOnError(e);
        service.recreateConnection();
        service.tryConnect();
    }

    private class NetworkExpectant extends Thread {

        @Override
        public void run() {
            super.run();

            while (!NetworkStatus.hasConnection(service)) {
                SystemClock.sleep(2000);
            }
        }
    }

    private class LogThread extends Thread
    {
        @Override
        public void run() {
            super.run();

            while (!isInterrupted()) {
                Log.d(TAG, "Has connection: " + NetworkStatus.hasConnection(service));
                Log.d(TAG, System.currentTimeMillis() + " : " +
                        "authenticated " + service.getConnection().isAuthenticated() + " | " +
                        "connected " + service.getConnection().isConnected()
                );

                if (!NetworkStatus.hasConnection(service) && service.getConnection().isConnected()) {
                    Log.d(TAG, "Start StubActivty and recreate service!");
                    service.recreateConnection();

                    Intent intent = new Intent(HVKZApp.getAppContext(), StubActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    HVKZApp.getAppContext().startActivity(intent);
                }

                SystemClock.sleep(30000);
            }
        }
    }
}
