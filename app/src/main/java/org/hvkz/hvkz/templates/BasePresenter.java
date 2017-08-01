package org.hvkz.hvkz.templates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.IBasePresenter;

@SuppressWarnings("unchecked")
public abstract class BasePresenter<T> implements IBasePresenter
{
    private ViewHandler<T> viewHandler;

    public BasePresenter(BaseWindow<T> activity) {
        this.viewHandler = createViewHandler(activity);
    }

    @Override
    public void init() {}

    protected void postUI(Runnable runnable) {
        viewHandler.postUI(runnable);
    }

    protected abstract ViewHandler<T> createViewHandler(BaseWindow<T> activity);

    @SuppressWarnings("UnusedParameters")
    protected <S extends ViewHandler<T>> S getViewHandler(Class<S> tClass) {
        return (S) viewHandler;
    }

    public  <S extends ViewHandler<T>> S getViewHandler() {
        return (S) viewHandler;
    }

    public Context context() {
        return viewHandler.context();
    }

    public Activity activity() {
        return viewHandler.activity();
    }

    public String string(@StringRes int resId) {
        return context().getString(resId);
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        //Stub
    }

    @Override
    public void onDestroy() {
        viewHandler.onDestroy();
    }
}
