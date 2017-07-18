package org.hvkz.hvkz.interfaces;

import android.app.Activity;
import android.content.Context;

import org.hvkz.hvkz.models.ViewBinder;

public abstract class ViewHandler implements Destroyable
{
    private BaseWindow window;

    public ViewHandler(BaseWindow baseWindow) {
        this.window = baseWindow;
        ViewBinder.handle(this, window.getActivity());
        handle(window.getContext());
    }

    public Context context() {
        return window.getContext();
    }

    public Activity activity() {
        return window.getActivity();
    }

    protected BaseWindow getWindow() {
        return window;
    }

    protected abstract void handle(Context context);

    @Override
    public void onDestroy() {
        window = null;
    }
}
