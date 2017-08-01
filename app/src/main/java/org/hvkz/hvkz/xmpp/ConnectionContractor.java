package org.hvkz.hvkz.xmpp;

import android.util.Log;

import org.hvkz.hvkz.event.Event;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.messaging.MessageReceiver;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;

import java.io.IOException;

import static org.hvkz.hvkz.firebase.db.GroupsStorage.REMOTE;

public class ConnectionContractor extends AbstractConnectionListener
{
    private static final String TAG = "ConnectionContractor";
    private ConnectionService service;

    public ConnectionContractor(ConnectionService service) {
        this.service = service;
    }

    @Override
    public void connected(XMPPConnection connection) {
        super.connected(connection);
        if (!connection.isAuthenticated()) {
            connection.addSyncStanzaListener(packet -> System.out.println(packet.toXML()), stanza -> true);

            ChatManager chatManager = service.getChatManager();
            Roster roster = service.getRoster();

            MessageReceiver messageReceiver = service.getMessageReceiver();

            roster.addRosterListener(messageReceiver);
            chatManager.addIncomingListener(messageReceiver);
            connection.addSyncStanzaListener(messageReceiver,
                    stanza -> stanza.getExtension(ChatStateExtension.NAMESPACE) != null);

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
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        super.authenticated(connection, resumed);
        ContextApp.getApp(service).getGroupsStorage().getMyGroups(REMOTE,
                value -> EventChannel.send(new Event<>(Event.EventType.GROUPS_DATA_WAS_CHANGED).setData(value))
        );
    }

    @Override
    public void connectionClosed() {
        super.connectionClosed();
        service.recreateConnection();
        service.tryConnect();
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        super.connectionClosedOnError(e);
        service.recreateConnection();
        service.tryConnect();
    }
}
