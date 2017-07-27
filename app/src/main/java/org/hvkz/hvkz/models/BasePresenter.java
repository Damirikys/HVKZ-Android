package org.hvkz.hvkz.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.interfaces.ViewHandler;

@SuppressWarnings("unchecked")
public abstract class BasePresenter<T> implements IBasePresenter
{
    private ViewHandler<T> viewHandler;

    public BasePresenter(BaseWindow<T> activity) {
        this.viewHandler = createViewHandler(activity);
    }

    @Override
    public void init() {}

    protected abstract ViewHandler<T> createViewHandler(BaseWindow<T> activity);

    public  <S extends ViewHandler<T>> S getViewHandler(Class<S> tClass) {
        return (S) viewHandler;
    }

    public  <S extends ViewHandler<T>> S getViewHandler() {
        return (S) viewHandler;
    }

    protected Context context() {
        return viewHandler.context();
    }

    protected Activity activity() {
        return viewHandler.activity();
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
