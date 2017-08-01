package org.hvkz.hvkz.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.modules.NavigationActivity;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uimodels.AppActivity;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.utils.validators.EmailValidator;

import javax.inject.Inject;

public class SyncPresenter extends BasePresenter<SyncPresenter> implements SyncCallback
{
    @Inject
    FirebaseUser firebaseUser;

    SyncPresenter(BaseWindow<SyncPresenter> baseWindow) {
        super(baseWindow);
        ContextApp.getApp(context()).component().inject(this);
    }

    private void startSync(String address) {
        SyncInteractor.with(context(), address)
                .call(this)
                .start();
    }

    @Override
    public void onSuccessSync(@NonNull User user) {
        firebaseUser.updateEmail(user.getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        askPassword(user);
                    } else {
                        onFailed(task.getException());
                    }
                });
    }

    private void askPassword(User user) {
        EditText passwordEditText = new EditText(context());
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        new BottomDialog.Builder(context())
                .setCancelable(false)
                .setTitle(R.string.enter_password)
                .setContent(R.string.password_notice)
                .setCustomView(passwordEditText)
                .setPositiveText(R.string.save)
                .autoDismiss(false)
                .onPositive(bottomDialog -> {
                    final String password = passwordEditText.getText().toString();
                    if (password.isEmpty()) return;
                    firebaseUser.updatePassword(password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                                    firebaseUser.reauthenticate(credential).addOnCompleteListener(task1 -> {
                                        activity().startActivity(new Intent(activity(), NavigationActivity.class));
                                        activity().finish();
                                    });
                                } else {
                                    Toast.makeText(context(), R.string.failed, Toast.LENGTH_SHORT).show();
                                }
                            });

                    bottomDialog.dismiss();
                }).show();
    }

    private void dialogMessage(String title, String message) {
        getViewHandler()
                .window(AppActivity.class)
                .dialogMessage(title, message);
    }


    @Override
    public void numberMismatch() {
        dialogMessage(string(R.string.wrong_data), string(R.string.phone_does_not_match));
    }

    @Override
    public void accountNotFound() {
        dialogMessage(string(R.string.not_found), string(R.string.account_email_not_exist));
    }

    @Override
    public void onFailed(Throwable throwable) {
        throwable.printStackTrace();
        if (NetworkStatus.hasConnection(context())) {
            dialogMessage(string(R.string.failed), string(R.string.sync_failed_check_phone));
        } else {
            dialogMessage(string(R.string.no_connected), string(R.string.check_connect_and_retry));
        }
    }

    @Override
    protected ViewHandler<SyncPresenter> createViewHandler(BaseWindow<SyncPresenter> activity) {
        return new ViewHandler<SyncPresenter>(activity)
        {
            @BindView(R.id.email_edit_text)
            private EditText emailEditText;

            @BindView(R.id.email_confirm_button)
            private Button emailConfirmButton;

            @Override
            protected void handle(Context context) {
                emailEditText.addTextChangedListener(new TextWatcherAdapter()
                {
                    @Override
                    public void afterTextChanged(Editable s) {
                        emailConfirmButton.setEnabled(EmailValidator.emailAddressIsCorrect(s.toString()));
                    }
                });
            }

            @OnClick(R.id.email_confirm_button)
            public void onEmailConfirm(View view) {
                if (EmailValidator.emailAddressIsCorrect(emailEditText.getText().toString())) {
                    activity.showProgress(string(R.string.please_wait));
                    SyncPresenter.this.startSync(emailEditText.getText().toString());
                }
            }
        };
    }
}
