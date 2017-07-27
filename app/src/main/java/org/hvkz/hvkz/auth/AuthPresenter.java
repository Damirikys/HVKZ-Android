package org.hvkz.hvkz.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.sync.SyncCallback;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.validators.EmailValidator;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hvkz.hvkz.auth.AuthActivity.ACTION_SMS_RECEIVE;

public class AuthPresenter extends BasePresenter<AuthPresenter> implements Destroyable, SyncCallback
{
    private static final String TAG = "AuthPresenter";
    private static final String SMS_EXTRA_KEY = "pdus";
    private static final int TIME_WAIT_LIMIT = 60;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider phoneAuthProvider;
    private OnVerificationState verificationState;
    private boolean isAuthenticate;

    public AuthPresenter(BaseWindow<AuthPresenter> view) {
        super(view);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.phoneAuthProvider = PhoneAuthProvider.getInstance();
        this.verificationState = new OnVerificationState();
    }

    @Override
    public void init() {
        if (firebaseAuth.getCurrentUser() != null) {
            SyncInteractor.with(context(), firebaseAuth.getCurrentUser().getEmail()).start();
            activity().startActivity(new Intent(context(), MainActivity.class));
            activity().finish();
        } else {
            activity().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "SMS WAS RECEIVED");
                    handleSMS(intent.getExtras());
                }
            }, new IntentFilter(ACTION_SMS_RECEIVE));
        }
    }

    public void verifyPhoneNumber(String number) {
        phoneAuthProvider.verifyPhoneNumber(number,
                TIME_WAIT_LIMIT,
                TimeUnit.SECONDS,
                activity(),
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

    public void verifySMSKey(final String key) {
        Log.d(TAG, "verifySMSKey:" + key);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationState.getVerificationToken(), key);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential");
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getViewHandler(AuthViewHandler.class).onAuthenticateSuccess();
            } else {
                if (!isAuthenticate) {
                    getViewHandler(AuthViewHandler.class)
                            .onAuthenticateFailed("Не удалось авторизоваться. Возможно," + "нет интернет-соединения или неправильно введен код.");
                }
            }
        });
    }

    public void signIn(String email, String password) {
        if (EmailValidator.emailAddressIsCorrect(email) && !password.isEmpty()) {
            getViewHandler().window().showProgress("Подождите...");
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                getViewHandler().window().hideProgress();
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    getViewHandler(AuthViewHandler.class).onAuthenticateSuccess();
                } else {
                    Log.d(TAG, "signInWithEmail:failed");
                    if (!isAuthenticate) {
                        getViewHandler(AuthViewHandler.class)
                                .onAuthenticateFailed("Не удалось авторизоваться. Возможно," + "нет интернет-соединения или неверный пароль.");
                    }
                }
            });
        }
    }

    public void syncAndContinue(String address) {
        SyncInteractor.with(context(), address)
                .call(this)
                .start();
    }

    @Override
    public void onSuccessSync(@NonNull User info) {
        Log.d(TAG, "OnSuccessSync");
        isAuthenticate = true;
        activity().startActivity(new Intent(activity(), MainActivity.class));
        activity().finish();
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
    }

    private void resetAuth() {
        firebaseAuth.signOut();
        activity().startActivity(new Intent(activity(), AuthActivity.class));
        activity().finish();
    }

    @Override
    protected ViewHandler<AuthPresenter> createViewHandler(BaseWindow<AuthPresenter> activity) {
        return new AuthViewHandler(activity);
    }
}
