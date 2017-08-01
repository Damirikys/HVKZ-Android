package org.hvkz.hvkz.xmpp.utils;

import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.xmpp.config.XMPPConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public abstract class JidFactory
{
    private JidFactory(){}

    public static EntityBareJid from(String groupName) {
        try {
            return JidCreate.entityBareFrom(groupName + "@" + XMPPConfiguration.DOMAIN_CONFERENCE);
        } catch (XmppStringprepException e) {
            return null;
        }
    }

    public static EntityBareJid from(User user) {
        try {
            return JidCreate.entityBareFrom(user.getUserId() + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            return null;
        }
    }
}
