package org.hvkz.hvkz.sync;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.app.IPresenter;
import org.hvkz.hvkz.uapi.models.UAPIClient;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.network.NetworkStatus;

import javax.inject.Inject;

public class SyncPresenter implements IPresenter, SyncCallback
{
    @Inject
    UAPIClient client;
    @Inject
    FirebaseUser firebaseUser;

    private AppActivity view;
    private SyncInteractor syncInteractor;

    public SyncPresenter(AppActivity view) {
        this.view = view;
        this.syncInteractor = new SyncInteractor(this);
        HVKZApp.component().inject(this);
    }

    public void startSync(String address) {
        client.getUser(address).enqueue(syncInteractor);
    }

    @Override
    public void onSuccessSync(@NonNull User user) {
        view.dialogMessage("Успешно", "Аккаунт успешно синхронизирован.");
        firebaseUser.updateEmail(user.getEmail());
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
