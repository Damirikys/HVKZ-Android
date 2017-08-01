package org.hvkz.hvkz.xmpp.config;

import org.hvkz.hvkz.xmpp.AbstractConnectionListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XMPPConfiguration
{
    public static final String SERVER = "http://api.hvkz.org";

    public static final String RESOURCE = "Android";
    public static final String DOMAIN = "s0565719c.fastvps-server.com";
    public static final String DOMAIN_CONFERENCE = "conference." + DOMAIN;
    public static final String HOST = "api.hvkz.org";
    public static final int PORT = 5222;

    static {
        SmackConfiguration.addDisabledSmackClass("org.jivesoftware.smackx.httpfileupload .HttpFileUploadManager");
    }

    private XMPPTCPConnectionConfiguration configuration;

    private XMPPConfiguration() {
        try {
            configuration = XMPPTCPConnectionConfiguration.builder()
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource(RESOURCE)
                    .setXmppDomain(DOMAIN)
                    .setHost(HOST)
                    .setPort(PORT)
                    .setCompressionEnabled(true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AbstractXMPPConnection connectionInstance(AbstractConnectionListener connectionListener) {
        XMPPTCPConnection connection = new XMPPTCPConnection(new XMPPConfiguration().configuration);
        connection.addConnectionListener(connectionListener);
        connection.setUseStreamManagement(true);
        connection.setUseStreamManagementResumption(true);

        return connection;
    }
}
