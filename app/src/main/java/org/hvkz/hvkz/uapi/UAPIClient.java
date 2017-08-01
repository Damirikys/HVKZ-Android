package org.hvkz.hvkz.uapi;

import android.support.annotation.NonNull;
import android.util.Log;

import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.entities.UnknownUser;
import org.hvkz.hvkz.uapi.entities.UserEntity;
import org.hvkz.hvkz.uapi.oauth.OAuthSignature;
import org.hvkz.hvkz.uapi.responses.UAPIUserResponse;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
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
    private static final String TAG = "UAPIClient";
    private static final String API_PREFIX = "/index/8-";
    private static final String API_POSTFIX = "?api";
    private static final String START_TAG = "START";
    private static final String END_TAG = "END";

    private final UAPIBackend uapiBackend;
    private final OkHttpClient okHttpClient;

    public UAPIClient() {
        this.okHttpClient = new OkHttpClient();
        this.uapiBackend = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UAPIBackend.class);
    }

    public void getUserById(int id, Callback<User> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + API_PREFIX + id + API_POSTFIX)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                callback.call(new UnknownUser());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String str = responseBody.string();
                    str = str.substring(str.indexOf(START_TAG) + START_TAG.length(), str.indexOf(END_TAG));
                    Log.d(TAG, str);
                    UserEntity entity = JSONFactory.fromJson(str, UserEntity.class);
                    callback.call(entity);
                } else {
                    callback.call(new UnknownUser());
                }
            }
        });
    }

    public Call<UAPIUserResponse> getUser(@NonNull String email) {
        try {
            String userEmail = URLEncoder.encode(email, ENCODING);
            String targetUrl = BASE_URL + UAPIBackend.GET_USER_BASE_URL;
            String nonce = Tools.nonce(Tools.NONCE_LENGTH);
            long timestamp = Tools.timestamp();
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
}
