package org.hvkz.hvkz.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.karan.churi.PermissionManager.PermissionManager;

import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.annotations.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Layout
public abstract class AppActivity<T extends IPresenter> extends AppCompatActivity
{
    private List<BroadcastReceiver> receivers = new ArrayList<>();
    private PermissionManager permissionManager;
    private ProgressDialog progressDialog;
    private T presenter;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<? extends AppActivity> clazz = getClass();
        setContentView(clazz.getAnnotation(Layout.class).value());

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(View.class)) {
                field.setAccessible(true);
                try {
                    field.set(this, findViewById(field.getAnnotation(View.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnClick.class)) {
                findViewById(method.getAnnotation(OnClick.class).value()).setOnClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(this));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                findViewById(method.getAnnotation(OnLongClick.class).value()).setOnLongClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(this));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                });
            }
        }

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

    public void showProgress(String message) {
        runOnUiThread(() -> {
            progressDialog.setMessage(message);
            progressDialog.show();
        });
    }

    public void hideProgress() {
        runOnUiThread(() -> progressDialog.dismiss());
    }

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
