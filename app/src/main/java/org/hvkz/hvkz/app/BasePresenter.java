package org.hvkz.hvkz.app;

import org.hvkz.hvkz.modules.home.ViewHandler;
import org.hvkz.hvkz.modules.home.ViewModel;

public abstract class BasePresenter implements Destroyable
{
    private ViewModel<BaseActivity> viewModel;

    public BasePresenter(BaseActivity activity) {
        this.viewModel = new ViewModel<>(activity);
        getViewHandler().handle(viewModel);
    }

    protected abstract ViewHandler getViewHandler();

    protected ViewModel<BaseActivity> getViewModel() {
        return viewModel;
    }

    @Override
    public void onDestroy() {
        viewModel.onDestroy();
    }
}
