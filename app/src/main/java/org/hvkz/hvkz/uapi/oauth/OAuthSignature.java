package org.hvkz.hvkz.uapi.oauth;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_CONSUMER_KEY;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_CONSUMER_SECRET;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_SIGNATURE_METHOD;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_TOKEN;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_TOKEN_SECRET;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_VERSION;

public class OAuthSignature
{
    private static final String TAG = "OAuthSignature";

    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    public static final String ENCODING = "UTF-8";

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static final String OAUTH_SIGNATURE_METHODE_NAME = "oauth_signature_method";
    private static final String OAUTH_CONSUMER_KEY_NAME = "oauth_consumer_key";
    private static final String OAUTH_TIMESTAMP_NAME = "oauth_timestamp";
    private static final String OAUTH_VERSION_NAME = "oauth_version";
    private static final String OAUTH_TOKEN_NAME = "oauth_token";
    private static final String OAUTH_NONCE_NAME = "oauth_nonce";

    private static final Map<String, String> defaultParams = new HashMap<String, String>() {{
        put(OAUTH_SIGNATURE_METHODE_NAME, OAUTH_SIGNATURE_METHOD);
        put(OAUTH_CONSUMER_KEY_NAME, OAUTH_CONSUMER_KEY);
        put(OAUTH_VERSION_NAME, OAUTH_VERSION);
        put(OAUTH_TOKEN_NAME, OAUTH_TOKEN);
    }};

    public static String create(String baseUrl,
                                String method,
                                String nonce,
                                long timestamp,
                                Map<String, String> extensions) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        Map<String, String> params = new HashMap<>(defaultParams);
        params.put(OAUTH_TIMESTAMP_NAME, String.valueOf(timestamp));
        params.put(OAUTH_NONCE_NAME, nonce);
        params.putAll(extensions);

        String base = method + "&" + URLEncoder.encode(baseUrl, ENCODING) + "&";

        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder paramsBuilder = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            paramsBuilder.append(keys.get(i))
                    .append("=")
                    .append(params.get(keys.get(i)));
            if (i + 1 != keys.size())
                paramsBuilder.append("&");
        }

        String value = base + URLEncoder.encode(paramsBuilder.toString(), ENCODING);
        String key =  OAUTH_CONSUMER_SECRET + "&" + OAUTH_TOKEN_SECRET;

        Log.d(TAG, "Base string: " + value);
        Log.d(TAG, "Key string: " + key);

        String signature = generate(value, key);

        Log.d(TAG, "Signature string: " + signature);

        return URLEncoder.encode(signature, ENCODING).replace("%0A", "");
    }

    public static String generate(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return Base64.encodeToString(mac.doFinal(data.getBytes()), Base64.DEFAULT);
    }
}