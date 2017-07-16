package org.hvkz.hvkz.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.pinball83.maskededittext.MaskedEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.models.AppActivity;
import org.hvkz.hvkz.sync.SyncActivity;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.utils.validators.NumberValidator;

@Layout(R.layout.activity_login)
public class AuthActivity extends AppActivity<AuthPresenter> implements AuthCallback
{
    public static final String TAG = "AuthActivity";
    public static final String ACTION_SMS_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";

    @BindView(R.id.phone_edit_text)
    MaskedEditText phoneEditText;

    @BindView(R.id.sign_in_button)
    Button signInButton;

    @BindView(R.id.splash_screen)
    FrameLayout splashScreen;

    private boolean codeSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HVKZApp.component().inject(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            onAuthenticateSuccess();
        } else {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "SMS WAS RECEIVED");
                    getPresenter().handleSMS(intent.getExtras());
                }
            }, new IntentFilter(ACTION_SMS_RECEIVE));

            phoneEditText.setOnKeyListener((v, keyCode, event) -> {
                if (!codeSend) {
                    signInButton.setEnabled(NumberValidator.numberIsCorrect(phoneEditText.getUnmaskedText()));
                }
                return false;
            });

            phoneEditText.setHintTextColor(Color.WHITE);
        }
    }

    @OnClick(R.id.sign_in_button)
    public void onSignButtonClick() {
        Log.d(TAG, "onSignButtonClick");
        if (NetworkStatus.hasConnection(this)) {
            signInButton.setEnabled(false);

            getPresenter().verifyPhoneNumber(phoneEditText.getUnmaskedText());
            codeSend = true;
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
                    codeSend = false;
                }
            }.start();
        } else {
            Toast.makeText(this, "Нет интернет соединения", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAuthenticateFailed(String desc) {
        Log.d(TAG, "onAuthenticateFailed");
        Toast.makeText(this, desc, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticateSuccess() {
        Log.d(TAG, "onAuthenticateSuccess");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "EMAIL: " + user.getEmail());
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                startActivity(new Intent(this, SyncActivity.class));
            } else {
                splashScreen.setVisibility(android.view.View.VISIBLE);
                getPresenter().syncAndContinue(user.getEmail());
            }
        }
    }

    @Override
    protected AuthPresenter createPresenter() {
        return new AuthPresenter(this);
    }
}

