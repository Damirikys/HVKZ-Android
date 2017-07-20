package org.hvkz.hvkz.adapters;

import android.support.annotation.NonNull;

import org.hvkz.hvkz.sync.SyncCallback;
import org.hvkz.hvkz.uapi.models.entities.User;

public abstract class SyncAdapter implements SyncCallback
{
    @Override
    public void onSuccessSync(@NonNull User info) {
    }

    @Override
    public void numberMismatch() {
        onFailed(new Exception("numberMismatch"));
    }

    @Override
    public void accountNotFound() {
        onFailed(new Exception("accountNotFound"));
    }

    @Override
    public void onFailed(Throwable throwable) {
        throwable.printStackTrace();
    }
}
