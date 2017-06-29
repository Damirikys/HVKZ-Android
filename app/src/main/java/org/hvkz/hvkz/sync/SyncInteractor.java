package org.hvkz.hvkz.sync;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.responses.UAPIUserResponse;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncInteractor implements Callback<UAPIUserResponse>
{
    private static final String TAG = "SyncInteractor";
    private static final int PHONE_LENGHT = 10;

    @Inject
    FirebaseUser firebaseUser;
    private SyncCallback callback;

    public SyncInteractor(SyncCallback callback) {
        this.callback = callback;
        HVKZApp.component().inject(this);
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
            callback.accountNotFound();
        }
    }

    @Override
    public void onFailure(@NonNull Call<UAPIUserResponse> call, @NonNull Throwable t) {
        callback.onFailed(t);
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
            callback.onSuccessSync(profile);
        } else {
            callback.numberMismatch();
        }
    }
}