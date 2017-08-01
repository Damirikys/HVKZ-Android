package org.hvkz.hvkz.modules.menu;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;

class MenuPresenter extends BasePresenter<MenuPresenter>
{
    MenuPresenter(BaseWindow<MenuPresenter> activity) {
        super(activity);
    }

    @Override
    protected ViewHandler<MenuPresenter> createViewHandler(BaseWindow<MenuPresenter> activity) {
        return new MenuViewHandler(activity);
    }
}
