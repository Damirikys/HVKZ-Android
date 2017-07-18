package org.hvkz.hvkz.xmpp;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.oauth.OAuthSignature;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class XMPPCredentials
{
    private static final XMPPCredentials CREDENTIALS = new XMPPCredentials();
    private static final String HMAC_DATA = "password";

    @Inject
    User user;

    private XMPPCredentials() {
        HVKZApp.component().inject(this);
    }

    public String getXmppLogin() {
        return String.valueOf(user.getUserId());
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

    public static XMPPCredentials getCredentials() {
        return CREDENTIALS;
    }
}
