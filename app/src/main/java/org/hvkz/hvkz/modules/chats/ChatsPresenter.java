package org.hvkz.hvkz.modules.chats;

import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;

public class ChatsPresenter extends BasePresenter
{
    public ChatsPresenter(BaseWindow activity) {
        super(activity);
    }

    @Override
    protected ViewHandler createViewHandler(BaseWindow activity) {
        return new ChatsViewHandler(activity);
    }
}
