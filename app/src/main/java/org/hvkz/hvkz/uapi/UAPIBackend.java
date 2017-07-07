package org.hvkz.hvkz.uapi;

import org.hvkz.hvkz.uapi.models.responses.UAPIPhotoResponse;
import org.hvkz.hvkz.uapi.models.responses.UAPIUserResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UAPIBackend
{
    String GET_USER_BASE_URL = "/uapi/users";
    String UPLOAD_PHOTO_BASE_URL = "/uapi/photo";

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

    @Multipart
    @POST(UPLOAD_PHOTO_BASE_URL)
    Call<UAPIPhotoResponse> uploadPhoto(
            @Query(value = "oauth_signature", encoded = true) String oauth_signature,
            @Query("oauth_signature_method") String oauth_signature_method,
            @Query("oauth_version") String oauth_version,
            @Query("oauth_consumer_key") String oauth_consumer_key,
            @Query("oauth_token") String oauth_token,
            @Query("oauth_nonce") String oauth_nonce,
            @Query("oauth_timestamp") long oauth_timestamp,
            @Query("category") int category_id,
            @Part("photo\"; filename=\"image.jpg\"") RequestBody file
    );
}
