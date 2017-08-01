package org.hvkz.hvkz.xmpp.config;

import org.hvkz.hvkz.uapi.oauth.OAuthSignature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class XMPPCredentials
{
    private static final String HMAC_DATA = "password";
    private static XMPPCredentials credentials;

    private int userId;

    private XMPPCredentials(int userId) {
        this.userId = userId;
    }

    public String getXmppLogin() {
        return String.valueOf(userId);
    }

    public String getXmppPassword() {
        try {
            return OAuthSignature.generate(HMAC_DATA, getXmppLogin());
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static XMPPCredentials getCredentials(int userId) {
        return credentials = new XMPPCredentials(userId);
    }

    public static XMPPCredentials getCredentials() {
        return credentials;
    }
}
