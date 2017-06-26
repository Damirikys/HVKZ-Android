package org.hvkz.hvkz.auth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.github.pinball83.maskededittext.MaskedEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.hvkz.hvkz.MainActivity;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.annotations.Layout;
import org.hvkz.hvkz.app.annotations.OnClick;
import org.hvkz.hvkz.app.annotations.View;

@Layout(R.layout.activity_login)
public class AuthActivity extends AppActivity<AuthPresenter> implements IAuthView
{
    public static final String TAG = "AuthActivity";
    public static final String ACTION_SMS_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";

    BroadcastReceiver smsReceiver;

    @View(R.id.phone_edit_text)
    MaskedEditText phoneEditText;
    @View(R.id.sign_in_button)
    Button signInButton;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            onAuthenticateSuccess();
        } else {
            phoneEditText.setOnKeyListener((v, keyCode, event) -> {
                signInButton.setEnabled(NumberValidator.numberIsCorrect(phoneEditText.getUnmaskedText()));
                return false;
            });

            registerReceiver(smsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "SMS WAS RECEIVED");
                    getPresenter().handleSMS(intent.getExtras());
                }
            }, new IntentFilter(ACTION_SMS_RECEIVE));
        }
    }

    @OnClick(R.id.sign_in_button)
    public void onSignButtonClick() {
        Log.d(TAG, "onSignButtonClick");
        signInButton.setEnabled(false);

        getPresenter().verifyPhoneNumber(phoneEditText.getUnmaskedText());

        new CountDownTimer(60000, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished) {
                String value = getString(R.string.repeat_through) + " " + String.valueOf(millisUntilFinished / 1000);
                signInButton.setText(value);
            }

            @Override
            public void onFinish() {
                signInButton.setText(getString(R.string.action_code_sent));
                signInButton.setEnabled(true);
            }
        }.start();
    }

    @Override
    public void onAuthenticateFailed(String desc) {
        Log.d(TAG, "onAuthenticateFailed");
        Toast.makeText(this, desc, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticateSuccess() {
        Log.d(TAG, "onAuthenticateSuccess");
        if (smsReceiver != null)
            unregisterReceiver(smsReceiver);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected AuthPresenter createPresenter() {
        return new AuthPresenter(this);
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}

