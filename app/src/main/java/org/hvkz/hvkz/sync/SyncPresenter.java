package org.hvkz.hvkz.sync;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.Destroyable;
import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.modules.home.MainActivity;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.network.NetworkStatus;

import javax.inject.Inject;

public class SyncPresenter implements Destroyable, SyncCallback
{
    @Inject
    FirebaseUser firebaseUser;

    private AppActivity view;

    public SyncPresenter(AppActivity view) {
        this.view = view;
        HVKZApp.component().inject(this);
    }

    public void startSync(String address) {
        SyncInteractor.with(address)
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
        EditText passwordEditText = new EditText(view);
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        new BottomDialog.Builder(view)
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
                                            view.startActivity(new Intent(view, MainActivity.class))
                                    );
                                } else {
                                    Toast.makeText(view, "Что-то пошло не так. Попробуйте снова.", Toast.LENGTH_SHORT).show();
                                }
                            });

                    bottomDialog.dismiss();
                }).show();
    }

    @Override
    public void numberMismatch() {
        view.dialogMessage("Неверные данные", "Ваш номер не совпадает с номером, указанным в аккаунте.");
    }

    @Override
    public void accountNotFound() {
        view.dialogMessage("Не найдено", "Аккаунт с таким E-mail адресом не найден.");
    }

    @Override
    public void onFailed(Throwable throwable) {
        throwable.printStackTrace();
        if (NetworkStatus.hasConnection(view)) {
            view.dialogMessage("Не удалось", "Не удалось синхронизироваться. Проверьте, что в аккаунте на сайте указан Ваш номер телефона.");
        } else {
            view.dialogMessage("Нет соединения", "Пожалуйста, проверьте подключение к интернету и повторите попытку.");
        }
    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
