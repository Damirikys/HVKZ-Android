package org.hvkz.hvkz.uimodels;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.karan.churi.PermissionManager.PermissionManager;

import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.utils.controllers.PermissionController;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2streamapi")
@Layout
public abstract class AppActivity<T extends IBasePresenter> extends AppCompatActivity implements BaseWindow<T>
{
    private List<BroadcastReceiver> receivers = new ArrayList<>();
    private PermissionManager permissionManager;
    private ProgressDialog progressDialog;
    private IBasePresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<? extends AppActivity> clazz = getClass();
        setContentView(clazz.getAnnotation(Layout.class).value());
        ViewBinder.handle(this, this);

        progressDialog = new ProgressDialog(this);
        permissionManager = PermissionController.checkAndRequestPermission(this);
        presenter = createPresenter();
        presenter.init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions, grantResults);
    }

    protected IBasePresenter  createPresenter() {
        return new StubPresenter();
    }

    @SuppressWarnings("unchecked")
    public T getPresenter() {
        if (presenter == null) presenter = createPresenter();
        return (T) presenter;
    }

    @Override
    public final Context getContext() {
        return this;
    }

    @Override
    public final Activity getActivity() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onResultReceive(requestCode, resultCode, data);
    }

    @Override
    public void showProgress(String message) {
        runOnUiThread(() -> {
            progressDialog.setMessage(message);
            progressDialog.show();
        });
    }

    @Override
    public void hideProgress() {
        runOnUiThread(() -> progressDialog.dismiss());
    }

    @Override
    public void dialogMessage(String title, String message) {
        runOnUiThread(() -> {
            if (progressDialog.isShowing()) hideProgress();
            new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage(message)
                            .create()
                            .show();
                }
        );
    }

    @Override
    public final Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        receivers.add(receiver);
        return super.registerReceiver(receivers.get(receivers.size() - 1), filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (BroadcastReceiver receiver : receivers)
            unregisterReceiver(receiver);
        receivers.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public Object getViewFinder() {
        return this;
    }

    private static final class StubPresenter implements IBasePresenter {

        @Override
        public void onDestroy() {}

        @Override
        public void init() {}

        @Override
        public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {}
    }
}
