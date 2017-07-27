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
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.AppActivity;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.network.NetworkStatus;
import org.hvkz.hvkz.utils.validators.EmailValidator;

import javax.inject.Inject;

public class SyncPresenter extends BasePresenter<SyncPresenter> implements SyncCallback
{
    @Inject
    FirebaseUser firebaseUser;

    public SyncPresenter(BaseWindow<SyncPresenter> baseWindow) {
        super(baseWindow);
        ContextApp.getApp(context()).component().inject(this);
    }

    public void startSync(String address) {
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
                .setTitle("Введите пароль")
                .setContent("Вы можете указать такой же пароль как для сайта, так и для приложения.")
                .setCustomView(passwordEditText)
                .setPositiveText("Сохранить")
                .autoDismiss(false)
                .onPositive(bottomDialog -> {
                    final String password = passwordEditText.getText().toString();
                    if (password.isEmpty()) return;
                    firebaseUser.updatePassword(password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                                    firebaseUser.reauthenticate(credential).addOnCompleteListener(task1 ->
                                            activity().startActivity(new Intent(activity(), MainActivity.class))
                                    );
                                } else {
                                    Toast.makeText(context(), "Что-то пошло не так. Попробуйте снова.", Toast.LENGTH_SHORT).show();
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
        dialogMessage("Неверные данные", "Ваш номер не совпадает с номером, указанным в аккаунте.");
    }

    @Override
    public void accountNotFound() {
        dialogMessage("Не найдено", "Аккаунт с таким E-mail адресом не найден.");
    }

    @Override
    public void onFailed(Throwable throwable) {
        throwable.printStackTrace();
        if (NetworkStatus.hasConnection(context())) {
            dialogMessage("Не удалось", "Не удалось синхронизироваться. Проверьте, что в аккаунте на сайте указан Ваш номер телефона.");
        } else {
            dialogMessage("Нет соединения", "Пожалуйста, проверьте подключение к интернету и повторите попытку.");
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
                    activity.showProgress("Подождите...");
                    SyncPresenter.this.startSync(emailEditText.getText().toString());
                }
            }
        };
    }
}
