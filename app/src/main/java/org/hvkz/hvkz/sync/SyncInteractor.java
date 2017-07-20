package org.hvkz.hvkz.sync;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.uapi.models.UAPIClient;
import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.responses.UAPIUserResponse;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class SyncInteractor
{
    private static final String TAG = "SyncInteractor";
    private static final int PHONE_LENGHT = 10;

    private SyncInteractor(){}

    public static SyncRequest with(String email) {
        return new SyncRequest(email);
    }

    public static class SyncRequest implements Callback<UAPIUserResponse>
    {
        @Inject
        UAPIClient client;

        @Inject
        FirebaseUser firebaseUser;

        private String address;
        private SyncCallback callback;

        private SyncRequest(String email) {
            HVKZApp.component().inject(this);
            this.address = email;
        }

        public SyncRequest call(SyncCallback callback) {
            this.callback = callback;
            return this;
        }

        public void start() {
            client.getUser(address).enqueue(this);
        }

        @Override
        public void onResponse(@NonNull Call<UAPIUserResponse> call,
                               @NonNull Response<UAPIUserResponse> response)
        {
            Log.d(TAG, "Response: " + response.toString());

            UAPIUserResponse uapiUserResponse = response.body();
            if (uapiUserResponse != null && uapiUserResponse.hasProfile()) {
                phoneVerify(uapiUserResponse.getUser());
            } else {
                if (callback != null) callback.accountNotFound();
            }
        }

        @Override
        public void onFailure(@NonNull Call<UAPIUserResponse> call, @NonNull Throwable t) {
            if (callback != null) callback.onFailed(t);
        }

        private void phoneVerify(User profile) {
            String uapiPhone = profile.getPhoneNumber();
            uapiPhone = uapiPhone.substring(uapiPhone.length() - PHONE_LENGHT);

            String firebasePhone = firebaseUser.getPhoneNumber();
            if (firebasePhone != null) {
                firebasePhone = firebasePhone.substring(firebasePhone.length() - PHONE_LENGHT);
            }

            Log.d(TAG, profile.toString());
            Log.d(TAG, uapiPhone + " " + firebasePhone);

            if (firebasePhone != null && firebasePhone.equals(uapiPhone)) {
                UAPIUser.setCurrentUser(profile);

                if (callback != null) callback.onSuccessSync(profile);
            } else {
                if (callback != null) callback.numberMismatch();
            }
        }
    }
}