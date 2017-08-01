package org.hvkz.hvkz.templates;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.uimodels.ViewBinder;

@SuppressWarnings("unchecked")
public abstract class ViewHandler<T> implements Destroyable
{
    private BaseWindow<T> window;
    private Handler handler;

    public ViewHandler(BaseWindow<T> baseWindow) {
        this.window = baseWindow;
        this.handler = new Handler(Looper.getMainLooper());
        ViewBinder.handle(this, window.getViewFinder());
        handle(window.getContext());
    }

    public String string(@StringRes int resId) {
        return context().getString(resId);
    }

    protected void postUI(Runnable runnable) {
        activity().runOnUiThread(runnable);
    }

    protected void postDelayedUI(Runnable runnable, int millis) {
        handler.postDelayed(runnable, millis);
    }

    public T presenter() {
        return window.getPresenter();
    }

    public Context context() {
        return window.getContext();
    }

    public Activity activity() {
        return window.getActivity();
    }

    public  <S extends BaseWindow<T>> S window() {
        return (S) window;
    }

    public <S> S window(Class<S> sClass) {
        return sClass.cast(window);
    }

    public BaseWindow<T> baseWindow() {
        return window;
    }

    protected abstract void handle(Context context);

    @Override
    public void onDestroy() {}
}
