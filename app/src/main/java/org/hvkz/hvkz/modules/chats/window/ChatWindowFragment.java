package org.hvkz.hvkz.modules.chats.window;

import android.os.Bundle;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.jxmpp.stringprep.XmppStringprepException;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

@Layout(R.layout.fragment_chat_window)
public class ChatWindowFragment extends AppFragment<ChatWindowPresenter>
{
    @Override
    protected ChatWindowPresenter bindPresenter() {
        Bundle bundle = getArguments();
        ChatType chatType = (ChatType) bundle.get(CHAT_TYPE_KEY);
        String address = bundle.getString(DOMAIN_KEY);

        try {
            return new ChatWindowPresenter(this, chatType, address);
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }
}
