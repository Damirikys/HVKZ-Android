package org.hvkz.hvkz.templates;

import android.os.Binder;

import java.lang.ref.WeakReference;

public class LocalBinder<S> extends Binder
{
    private final WeakReference<S> mService;

    public LocalBinder(final S service) {
        mService = new WeakReference<>(service);
    }

    public S getService() {
        return mService.get();
    }
}