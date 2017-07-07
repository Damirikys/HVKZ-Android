package org.hvkz.hvkz.app;

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

import java.util.ArrayList;
import java.util.List;

@Layout
public abstract class AppActivity<T extends Destroyable> extends AppCompatActivity implements BaseActivity
{
    private List<BroadcastReceiver> receivers = new ArrayList<>();
    private PermissionManager permissionManager;
    private ProgressDialog progressDialog;
    private T presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<? extends AppActivity> clazz = getClass();
        setContentView(clazz.getAnnotation(Layout.class).value());
        ActivityHolder.hold(this, this);

        progressDialog = new ProgressDialog(this);
        permissionManager = PermissionController.checkAndRequestPermission(this);

        presenter = createPresenter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions, grantResults);
    }

    protected abstract T createPresenter();

    protected T getPresenter() {
        if (presenter == null) {
            presenter = createPresenter();
        }

        return presenter;
    }

    @Override
    public final Context getContext() {
        return getBaseContext();
    }

    @Override
    public final Activity getActivity() {
        return this;
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
}
