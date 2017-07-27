package org.hvkz.hvkz.interfaces;

import android.app.Activity;
import android.content.Context;

import org.hvkz.hvkz.models.ViewBinder;

public abstract class ViewHandler<T> implements Destroyable
{
    private BaseWindow<T> window;

    public ViewHandler(BaseWindow<T> baseWindow) {
        this.window = baseWindow;
        ViewBinder.handle(this, window.getActivity());
        handle(window.getContext());
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

    public <S> S window(Class<S> sClass) {
        return sClass.cast(window);
    }

    public BaseWindow<T> window() {
        return window;
    }

    protected abstract void handle(Context context);

    @Override
    public void onDestroy() {}
}
