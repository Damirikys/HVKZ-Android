package org.hvkz.hvkz.sync;

import android.support.annotation.NonNull;

import org.hvkz.hvkz.uapi.User;

public interface SyncCallback
{
    void onSuccessSync(@NonNull User info);

    void numberMismatch();

    void accountNotFound();

    void onFailed(Throwable throwable);
}
