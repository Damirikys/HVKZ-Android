package org.hvkz.hvkz.modules.chats.window.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.uimodels.AppFragment;
import org.jxmpp.stringprep.XmppStringprepException;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

@Layout(R.layout.fragment_chat_window)
public class ChatWindowFragment extends AppFragment<ChatWindowPresenter>
{
    public static final int GALLERY_REQUEST = 2;

    @BindView(R.id.toolbar)
    private Toolbar toolbar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationIcon(R.drawable.arrow_left);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

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

    @Override
    public void onBackPressed() {
        getParentActivity().onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenter().onChatShowed();
    }

    @Override
    public void onStop() {
        super.onStop();
        getPresenter().onChatHidden();
    }
}
