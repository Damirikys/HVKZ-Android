package org.hvkz.hvkz.modules.chats.window;

import android.content.Intent;
import android.widget.Toast;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static org.hvkz.hvkz.modules.chats.window.ChatWindowFragment.GALLERY_REQUEST;

@SuppressWarnings("unchecked")
public class ChatWindowPresenter extends BasePresenter<ChatWindowPresenter> implements ChatWindow
{
    @Inject
    User user;

    private String domain;
    private ChatWindowViewHandler viewHandler;
    private ChatDisposer chatDisposer;

    public ChatWindowPresenter(BaseWindow<ChatWindowPresenter> window, ChatType chatType, String localpart) throws XmppStringprepException {
        super(window);
        HVKZApp app = ContextApp.getApp(window.getContext());
        app.component().inject(this);

        this.domain = localpart;

        ChatDisposer.obtain(disposer -> {
            if (viewHandler != null) {
                if (chatDisposer != null) chatDisposer.onDestroy();
                chatDisposer = disposer;
                MessagesListAdapter adapter = new MessagesListAdapter(app, chatDisposer);
                adapter.setOnSelectMessageListener(viewHandler);
                viewHandler.getMessagesListView().setMessagesAdapter(adapter);
                viewHandler.init(chatDisposer);
            }
        }, context(), chatType, localpart);
    }

    @Override
    public void sendMessage(String message) {
        try {
            ChatMessage chatMessage = new ChatMessage(context())
                    .setBody(message)
                    .setTimestamp(System.currentTimeMillis())
                    .setImages(viewHandler.getImageAttachments())
                    .setForwarded(viewHandler.getForwardedMessages())
                    .setSenderId(user.getUserId())
                    .setChatJid(chatDisposer.chatJid);

            chatDisposer.sendMessage(chatMessage);
            viewHandler.clearAll();
            viewHandler.getMessagesListView().addNewMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            Toast.makeText(context(), "Нет соединения с сервером", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(context(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    public void onChatHidden() {
        ContextApp.getApp(context()).getNotificationService().unlock(domain);
    }

    public void onChatShowed() {
        ContextApp.getApp(context()).getNotificationService().lock(domain);
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        super.onResultReceive(requestCode, resultCode, dataIntent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    onSelectedImage(dataIntent);
                    break;
            }
        }
    }

    private void onSelectedImage(Intent data) {
        viewHandler.attachPhoto(data.getData());
    }

    @Override
    protected ViewHandler createViewHandler(BaseWindow<ChatWindowPresenter> activity) {
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
