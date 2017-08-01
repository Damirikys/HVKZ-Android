package org.hvkz.hvkz.modules.chats;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;

class ChatsPresenter extends BasePresenter<ChatsPresenter>
{
    ChatsPresenter(BaseWindow<ChatsPresenter> activity) {
        super(activity);
    }

    @Override
    protected ViewHandler<ChatsPresenter> createViewHandler(BaseWindow<ChatsPresenter> activity) {
        return new ChatsViewHandler(activity);
    }
}
