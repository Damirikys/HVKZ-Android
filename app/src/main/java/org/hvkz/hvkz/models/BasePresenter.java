package org.hvkz.hvkz.models;

import android.content.Context;

import org.hvkz.hvkz.interfaces.BaseActivity;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.interfaces.ViewHandler;

public abstract class BasePresenter implements IBasePresenter
{
    private ViewModel<BaseActivity> viewModel;

    public BasePresenter(BaseActivity activity) {
        this.viewModel = new ViewModel<>(activity);
        getViewHandler().handle(viewModel);
    }

    protected abstract ViewHandler getViewHandler();

    protected Context getContext() {
        return viewModel.context();
    }

    protected ViewModel<BaseActivity> getViewModel() {
        return viewModel;
    }

    @Override
    public void onDestroy() {
        viewModel.onDestroy();
    }
}
