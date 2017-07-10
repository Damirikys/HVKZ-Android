package org.hvkz.hvkz.utils.controllers;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import com.karan.churi.PermissionManager.PermissionManager;

import org.hvkz.hvkz.R;

public class PermissionController extends PermissionManager
{
    @Override
    public void ifCancelledAndCanRequest(Activity activity) {
        new AlertDialog.Builder(activity)
            .setMessage(activity.getString(R.string.permission_denied_description))
                .setOnCancelListener(dialog -> checkAndRequestPermission(activity))
            .create()
            .show();
    }

    @Override
    public void ifCancelledAndCannotRequest(Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.permission_denied_cannot_description))
                .setOnCancelListener(dialog -> activity.finish())
                .create()
                .show();
    }

    public static PermissionManager checkAndRequestPermission(Activity activity) {
        PermissionController controller = new PermissionController();
        controller.checkAndRequestPermissions(activity);
        return controller;
    }
}
