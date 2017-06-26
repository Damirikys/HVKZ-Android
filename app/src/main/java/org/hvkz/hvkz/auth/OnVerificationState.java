package org.hvkz.hvkz.auth;

import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OnVerificationState extends PhoneAuthProvider.OnVerificationStateChangedCallbacks
{
    public static final String TAG = "OnVerificationState";

    private String verificationToken;

    @Override
    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
        Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential.toString());
    }

    @Override
    public void onVerificationFailed(FirebaseException e) {
        e.printStackTrace();
    }

    @Override
    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        super.onCodeSent(s, forceResendingToken);
        Log.d(TAG, "onCodeSent: " + s);
        verificationToken = s;
    }

    public String getVerificationToken() {
        return verificationToken;
    }
}
