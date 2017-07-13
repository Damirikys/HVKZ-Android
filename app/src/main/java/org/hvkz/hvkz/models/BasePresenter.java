package org.hvkz.hvkz.models;

import android.content.Context;
import android.content.Intent;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.interfaces.ViewHandler;

@SuppressWarnings("unchecked")
public abstract class BasePresenter implements IBasePresenter
{
    private ViewHandler viewHandler;

    public BasePresenter(BaseWindow activity) {
        this.viewHandler = createViewHandler(activity);
    }

    protected abstract ViewHandler createViewHandler(BaseWindow activity);

    protected <T extends ViewHandler> T getViewHandler(Class<T> tClass) {
        return (T) viewHandler;
    }

    protected Context getContext() {
        return viewHandler.context();
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
