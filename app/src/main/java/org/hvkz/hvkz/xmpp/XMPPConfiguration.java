package org.hvkz.hvkz.xmpp;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XMPPConfiguration
{
    public static final String SERVER = "http://api.hvkz.org";

    public static final String RESOURCE = "Android";
    public static final String DOMAIN = "s0565719c.fastvps-server.com";
    public static final String DOMAIN_CONFERENCE = "conference.s0565719c.fastvps-server.com";
    public static final String HOST = "api.hvkz.org";
    public static final int PORT = 5222;

    private XMPPTCPConnectionConfiguration configuration;

    private XMPPConfiguration() {
        try {
            XMPPCredentials credentials = XMPPCredentials.getCredentials();
            configuration = XMPPTCPConnectionConfiguration.builder()
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource(RESOURCE)
                    .setXmppDomain(DOMAIN)
                    .setHost(HOST)
                    .setPort(PORT)
                    .setUsernameAndPassword(credentials.getXmppLogin(), credentials.getXmppPassword())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AbstractXMPPConnection connectionInstance(AbstractConnectionListener connectionListener) {
        AbstractXMPPConnection connection = new XMPPTCPConnection(new XMPPConfiguration().configuration);
        connection.addConnectionListener(connectionListener);
        return connection;
    }
}
