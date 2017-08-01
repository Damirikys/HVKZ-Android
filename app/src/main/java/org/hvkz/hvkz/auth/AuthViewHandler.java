package org.hvkz.hvkz.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scout.widget.MaskedEditText;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.modules.NavigationActivity;
import org.hvkz.hvkz.sync.SyncActivity;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.UAPIClient;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.utils.validators.NumberValidator;

import static org.hvkz.hvkz.auth.AuthActivity.ACTION_SMS_RECEIVE;

public class AuthViewHandler extends ViewHandler<AuthPresenter> implements AuthCallback
{
    public static final String TAG = "AuthViewHandler";

    private UAPIClient uapiClient;

    @BindView(R.id.email_login_form)
    private View emailLoginForm;

    @BindView(R.id.phone_login_form)
    private View phoneLoginForm;

    @BindView(R.id.phone_edit_text)
    private MaskedEditText phoneEditText;

    @BindView(R.id.code_edit_text)
    private EditText codeEditText;

    @BindView(R.id.password_input)
    private EditText passwordEditText;

    @BindView(R.id.email_input)
    private EditText emailEditText;

    @BindView(R.id.phone_input_layout)
    private TextInputLayout phoneInputLayout;

    @BindView(R.id.code_input_layout)
    private TextInputLayout codeInputLayout;

    @BindView(R.id.sign_in_button)
    private Button signInButton;

    @BindView(R.id.splash_screen)
    private FrameLayout splashScreen;

    private boolean codeSent;

    public AuthViewHandler(BaseWindow<AuthPresenter> baseWindow) {
        super(baseWindow);
    }

    @Override
    protected void handle(Context context) {
        HVKZApp app = ContextApp.getApp(context);
        app.component().inject(this);
        uapiClient = app.getUAPIclient();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            splashScreen.setVisibility(View.VISIBLE);
            uapiClient.getUserById(app.getCurrentUser().getUserId(), user -> {
                if (user != null) app.setCurrentUser(user);
                activity().startActivity(new Intent(context(), NavigationActivity.class));
                activity().finish();
            });
        } else {
            activity().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "SMS WAS RECEIVED");
                    presenter().handleSMS(intent.getExtras());
                }
            }, new IntentFilter(ACTION_SMS_RECEIVE));

            phoneEditText.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    super.afterTextChanged(s);
                    if (!codeSent) {
                        signInButton.setEnabled(NumberValidator.numberIsCorrect(phoneEditText.getUnmaskedText().toString()));
                    }
                }
            });

            codeEditText.setOnKeyListener((v, keyCode, event) -> {
                if (codeEditText.getText().toString().length() == 6) {
                    presenter().verifySMSKey(codeEditText.getText().toString());
                }

                return false;
            });

            phoneEditText.setHintTextColor(Color.WHITE);
            codeEditText.setHintTextColor(Color.WHITE);
        }
    }


    @OnClick(R.id.sign_in_button)
    public void onSignButtonClick(View view) {
        if (NetworkStatus.hasConnection(context())) {
            presenter().verifyPhoneNumber(phoneEditText.getUnmaskedText().toString());

            Toast.makeText(context(),
                    "Код отправлен. Пожалуйста, не сворачивайте экран, пока не придет СМС.", Toast.LENGTH_LONG).show();

            phoneInputLayout.setVisibility(View.GONE);
            codeInputLayout.setVisibility(View.VISIBLE);

            signInButton.setEnabled(false);

            codeSent = true;

            new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String value = context().getString(R.string.repeat_through) + " " + String.valueOf(millisUntilFinished / 1000);
                    signInButton.setText(value);
                }

                @Override
                public void onFinish() {
                    signInButton.setText(context().getString(R.string.action_code_sent));
                    signInButton.setEnabled(true);
                    phoneInputLayout.setVisibility(View.VISIBLE);
                    codeEditText.getText().clear();
                    codeInputLayout.setVisibility(View.GONE);
                    codeSent = false;
                }
            }.start();
        } else {
            Toast.makeText(context(), "Нет интернет соединения", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.enter_with_email)
    public void onEmailLoginClick(View view) {
        phoneLoginForm.setVisibility(View.INVISIBLE);
        emailLoginForm.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.enter_with_phone)
    public void onPhoneLoginClick(View view) {
        emailLoginForm.setVisibility(View.INVISIBLE);
        phoneLoginForm.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.emailEnterButton)
    public void onSignWithEmailClick(View view) {
        presenter().signIn(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString()
        );
    }

    @Override
    public void onAuthenticateFailed(String desc) {
        Log.d(TAG, "onAuthenticateFailed");
        Toast.makeText(context(), desc, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticateSuccess() {
        Log.d(TAG, "onAuthenticateSuccess");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "EMAIL: " + user.getEmail());
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                activity().startActivity(new Intent(activity(), SyncActivity.class));
            } else {
                splashScreen.setVisibility(android.view.View.VISIBLE);
                presenter().syncAndContinue(user.getEmail());
            }
        }
    }
}
