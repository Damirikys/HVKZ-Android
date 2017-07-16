package org.hvkz.hvkz.modules.chats.window;

import android.widget.Toast;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smack.SmackException;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.inject.Inject;

@SuppressWarnings("unchecked")
public class ChatWindowPresenter extends BasePresenter implements ChatWindow
{
    @Inject
    User user;

    private MessagesStorage messagesStorage;
    private ChatWindowViewHandler viewHandler;
    private ChatDisposer chatDisposer;
    private ConnectionService service;

    public ChatWindowPresenter(BaseWindow window, ChatType chatType, String domain) throws XmppStringprepException {
        super(window);
        HVKZApp.component().inject(this);
        this.service = ((MainActivity) window.getActivity()).getConnectionService();
        this.messagesStorage = MessagesStorage.getInstance();
        this.chatDisposer = ChatDisposer.obtain(service, chatType, domain);
        this.viewHandler.init(chatDisposer);
    }

    @Override
    public void sendMessage(String message) {
        try {
            ChatMessage chatMessage = new ChatMessage()
                    .setBody(message)
                    .setTimestamp(System.currentTimeMillis())
                    .setImages(viewHandler.getImageAttachments())
                    .setForwarded(viewHandler.getForwardedMessages())
                    .setSenderId(user.getUserId())
                    .setRead(false);

            chatDisposer.sendMessage(chatMessage);
            viewHandler.clearAll();
            viewHandler.getMessagesListView().addNewMessage(chatMessage);
        } catch (SmackException.NotConnectedException e) {
            Toast.makeText(getContext(), "Нет соединения", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Произошла ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected ViewHandler createViewHandler(BaseWindow activity) {
        return viewHandler = new ChatWindowViewHandler(this, activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatDisposer.onDestroy();
        viewHandler = null;
        chatDisposer = null;
        service = null;
    }
}
