package org.hvkz.hvkz.xmpp.notification_service;

import android.util.Log;

import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.AbstractConnectionListener;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.hvkz.hvkz.xmpp.message_service.MessageReceiver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
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

import static org.hvkz.hvkz.firebase.db.groups.GroupsStorage.REMOTE;

public class ConnectionServiceController extends AbstractConnectionListener
{
    private static final String TAG = "ConnectionController";
    private ConnectionService service;

    public ConnectionServiceController(ConnectionService service) {
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
        ContextApp.getApp(service.getApplication())
                .getGroupsStorage()
                .getMyGroups(REMOTE, groups -> {
                    MultiUserChatManager MUCmanager = service.getMUCmanager();
                    for (Group group : groups) {
                        try {
                            MultiUserChat multiUserChat = MUCmanager.getMultiUserChat(JidCreate.entityBareFrom(
                                    Localpart.from(group.getGroupName()),
                                    Domainpart.from(XMPPConfiguration.DOMAIN_CONFERENCE)
                            ));

                            multiUserChat.addMessageListener(service.getMessageReceiver());
                            ChatMessage lastMessage = ContextApp.getApp(service).getMessagesStorage()
                                    .getLastMessage(multiUserChat.getRoom());

                            MucEnterConfiguration.Builder configBuilder = multiUserChat
                                    .getEnterConfigurationBuilder(
                                            Resourcepart.from(service.getCredentials().getXmppLogin()));

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
