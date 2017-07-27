package org.hvkz.hvkz.utils;

import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.XMPPConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public abstract class JidFactory
{
    private JidFactory(){}

    public static EntityBareJid from(User user) {
        try {
            return JidCreate.entityBareFrom(user.getUserId() + "@" + XMPPConfiguration.DOMAIN);
        } catch (XmppStringprepException e) {
            return null;
        }
    }
}
