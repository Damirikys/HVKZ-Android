package org.hvkz.hvkz.xmpp;

import org.hvkz.hvkz.uapi.oauth.OAuthSignature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String hmacPassword = OAuthSignature.generate(HMAC_DATA, getXmppLogin());
            Pattern intsOnly = Pattern.compile("\\d+");
            Matcher makeMatch = intsOnly.matcher(hmacPassword);
            makeMatch.find();

            System.out.println("HMAC PASSWORD " + hmacPassword);

            return makeMatch.group();
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
