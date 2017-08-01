package org.hvkz.hvkz.modules.chats.window.ui;

import android.content.Intent;
import android.widget.Toast;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.modules.chats.window.ChatWindow;
import org.hvkz.hvkz.modules.chats.window.messages.MessagesListAdapter;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.utils.controllers.NotificationController;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

@SuppressWarnings("WeakerAccess")
public class ChatWindowPresenter extends BasePresenter<ChatWindowPresenter> implements ChatWindow
{
    @Inject
    User user;

    @Inject
    NotificationController notificationController;

    private String localpart;
    private ChatWindowViewHandler viewHandler;
    private ChatDisposer chatDisposer;

    ChatWindowPresenter(BaseWindow<ChatWindowPresenter> window, ChatType chatType, String localpart) throws XmppStringprepException {
        super(window);
        ContextApp.getApp(window.getContext()).component().inject(this);

        this.localpart = localpart;

        ChatDisposer.obtain(disposer -> postUI(() -> {
            if (viewHandler != null) {
                if (chatDisposer != null) chatDisposer.onDestroy();
                chatDisposer = disposer;
                MessagesListAdapter adapter = new MessagesListAdapter(window.getContext(), chatDisposer);
                adapter.setOnSelectMessageListener(viewHandler);
                viewHandler.getMessagesListView().setMessagesAdapter(adapter);
                viewHandler.init(chatDisposer);
            }
        }), this, chatType, localpart);
    }

    @Override
    public void sendMessage(String message) {
        try {
            ChatMessage chatMessage = new ChatMessage(context())
                    .setBody(message)
                    .setTimestamp(Tools.timestamp())
                    .setImages(viewHandler.getImageAttachments())
                    .setForwarded(viewHandler.getForwardedMessages())
                    .setSenderId(user.getUserId())
                    .setChatJid(chatDisposer.getChatJid());

            chatDisposer.sendMessage(chatMessage);
            viewHandler.clearAll();
            viewHandler.getMessagesListView().addNewMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            Toast.makeText(context(), R.string.no_connected, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(context(), R.string.unknown_warning, Toast.LENGTH_SHORT).show();
        }
    }

    void onChatHidden() {
        notificationController.unlock(localpart);
    }

    void onChatShowed() {
        notificationController.lock(localpart);
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        super.onResultReceive(requestCode, resultCode, dataIntent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ChatWindowFragment.GALLERY_REQUEST:
                    onSelectedImage(dataIntent);
                    break;
            }
        }
    }

    private void onSelectedImage(Intent data) {
        viewHandler.attachPhoto(data.getData());
    }

    @Override
    protected ViewHandler<ChatWindowPresenter> createViewHandler(BaseWindow<ChatWindowPresenter> activity) {
        return viewHandler = new ChatWindowViewHandler(this, activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatDisposer.onDestroy();
        viewHandler = null;
        chatDisposer = null;
    }
}
