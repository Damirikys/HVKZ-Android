package org.hvkz.hvkz.uapi;

import org.hvkz.hvkz.uapi.responses.UAPIUserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface UAPIBackend
{
    String GET_USER_BASE_URL = "/uapi/users";

    @GET(GET_USER_BASE_URL)
    Call<UAPIUserResponse> getUser(
            @Query(value = "oauth_signature", encoded = true) String oauth_signature,
            @Query("oauth_signature_method") String oauth_signature_method,
            @Query("oauth_version") String oauth_version,
            @Query("oauth_consumer_key") String oauth_consumer_key,
            @Query("oauth_token") String oauth_token,
            @Query("oauth_nonce") String oauth_nonce,
            @Query("oauth_timestamp") long oauth_timestamp,
            @Query(value = "user", encoded = true) String email
    );
}
