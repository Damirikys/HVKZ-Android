package org.hvkz.hvkz.uapi.models;

import android.support.annotation.NonNull;

import org.hvkz.hvkz.uapi.UAPIBackend;
import org.hvkz.hvkz.uapi.models.responses.UAPIUserResponse;
import org.hvkz.hvkz.uapi.oauth.OAuthSignature;
import org.hvkz.hvkz.uapi.utils.NonceGenerator;

import java.net.URLEncoder;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.hvkz.hvkz.uapi.oauth.OAuth.BASE_URL;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_CONSUMER_KEY;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_SIGNATURE_METHOD;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_TOKEN;
import static org.hvkz.hvkz.uapi.oauth.OAuth.OAUTH_VERSION;
import static org.hvkz.hvkz.uapi.oauth.OAuthSignature.ENCODING;

public class UAPIClient
{
    private static final UAPIClient uapiClient = new UAPIClient();

    private final UAPIBackend uapiBackend;

    private UAPIClient() {
        uapiBackend = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UAPIBackend.class);
    }

    public Call<UAPIUserResponse> getUser(@NonNull String email) {
        try {
            String userEmail = URLEncoder.encode(email, ENCODING);
            String targetUrl = BASE_URL + UAPIBackend.GET_USER_BASE_URL;
            String nonce = NonceGenerator.nonce();
            long timestamp = getTimestamp();
            String signature = OAuthSignature.create(
                    targetUrl,
                    OAuthSignature.GET,
                    nonce,
                    timestamp,
                    new HashMap<String, String>() {{
                        put("user", userEmail);
                    }}
            );

            return uapiBackend.getUser(
                    signature,
                    OAUTH_SIGNATURE_METHOD,
                    OAUTH_VERSION,
                    OAUTH_CONSUMER_KEY,
                    OAUTH_TOKEN,
                    nonce,
                    timestamp,
                    userEmail
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static UAPIClient getInstance() {
        return uapiClient;
    }
}
