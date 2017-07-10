package org.hvkz.hvkz.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.sync.SyncCallback;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthPresenter implements Destroyable, SyncCallback
{
    private static final String TAG = "AuthPresenter";
    private static final String SMS_EXTRA_KEY = "pdus";
    private static final int TIME_WAIT_LIMIT = 60;

    private AuthCallback view;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider phoneAuthProvider;

    private OnVerificationState verificationState;

    public AuthPresenter(AuthCallback view) {
        this.view = view;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.phoneAuthProvider = PhoneAuthProvider.getInstance();
        this.verificationState = new OnVerificationState();
    }

    public void verifyPhoneNumber(String number) {
        phoneAuthProvider.verifyPhoneNumber(number,
                TIME_WAIT_LIMIT,
                TimeUnit.SECONDS,
                view.getActivity(),
                verificationState
        );
    }

    public void handleSMS(Bundle extras) {
        if (extras != null) {
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_KEY);
            StringBuilder bodyBuilder = new StringBuilder();
            if (smsExtra != null) {
                for (Object aSmsExtra : smsExtra) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) aSmsExtra);
                    bodyBuilder.append(sms.getMessageBody());
                }

                Pattern intsOnly = Pattern.compile("\\d+");
                Matcher makeMatch = intsOnly.matcher(bodyBuilder.toString());
                if (makeMatch.find()) {
                    verifySMSKey(makeMatch.group());
                }
            }
        }
    }

    private void verifySMSKey(String key) {
        Log.d(TAG, "verifySMSKey:" + key);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationState.getVerificationToken(), key);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential");
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                view.onAuthenticateSuccess();
            } else {
                view.onAuthenticateFailed("Не удалось авторизоваться. Проверьте интернет-соединение и повторите попытку.");
            }
        });
    }

    public void syncAndContinue(String address) {
        SyncInteractor.with(address)
                .call(this)
                .start();
    }

    @Override
    public void onSuccessSync(@NonNull User info) {
        view.getActivity().startActivity(new Intent(view.getActivity(), MainActivity.class));
        view.getActivity().finish();
    }

    @Override
    public void numberMismatch() {
        resetAuth();
    }

    @Override
    public void accountNotFound() {
        resetAuth();
    }

    @Override
    public void onFailed(Throwable throwable) {
        throwable.printStackTrace();
        resetAuth();
    }

    private void resetAuth() {
        FirebaseAuth.getInstance().signOut();
        view.getActivity().startActivity(new Intent(view.getActivity(), AuthActivity.class));
        view.getActivity().finish();
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
