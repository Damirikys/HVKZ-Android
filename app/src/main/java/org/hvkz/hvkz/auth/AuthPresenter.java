package org.hvkz.hvkz.auth;

import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthPresenter
{
    private static final String TAG = "AuthPresenter";
    private static final String SMS_EXTRA_KEY = "pdus";
    private static final int TIME_WAIT_LIMIT = 60;

    private IAuthView view;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider phoneAuthProvider;

    private OnVerificationState verificationState;

    public AuthPresenter(IAuthView view) {
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
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                view.onAuthenticateSuccess();
            } else {
                view.onAuthenticateFailed("Не удалось авторизоваться. Проверьте интернет-соединение и повторите попытку.");
            }
        });
    }
}
