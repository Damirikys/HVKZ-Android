package org.hvkz.hvkz.modules.chats;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;

public class ChatsPresenter extends BasePresenter<ChatsPresenter>
{
    public ChatsPresenter(BaseWindow<ChatsPresenter> activity) {
        super(activity);
    }

    @Override
    protected ViewHandler<ChatsPresenter> createViewHandler(BaseWindow<ChatsPresenter> activity) {
        return new ChatsViewHandler(activity);
    }
}
