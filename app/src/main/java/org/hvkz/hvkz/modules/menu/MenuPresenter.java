package org.hvkz.hvkz.modules.menu;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;

public class MenuPresenter extends BasePresenter<MenuPresenter>
{
    public MenuPresenter(BaseWindow<MenuPresenter> activity) {
        super(activity);
    }

    @Override
    protected ViewHandler<MenuPresenter> createViewHandler(BaseWindow<MenuPresenter> activity) {
        return new MenuViewHandler(activity);
    }
}
