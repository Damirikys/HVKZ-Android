package org.hvkz.hvkz.modules.chats.window;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.modules.chats.DateChangeListener;
import org.hvkz.hvkz.modules.chats.MessagesListView;
import org.hvkz.hvkz.utils.Animations;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.List;

public class ChatWindowViewHandler extends ViewHandler implements DateChangeListener
{
    @BindView(R.id.buddy_avatar)
    private ImageView photoView;

    @BindView(R.id.buddy_name)
    private TextView titleView;

    @BindView(R.id.buddy_status)
    private TextView statusView;

    @BindView(R.id.datetime_view)
    private View dateTimeView;

    @BindView(R.id.datetime)
    private TextView dateTimeTextView;

    @BindView(R.id.msgListView)
    private MessagesListView messagesListView;

    @BindView(R.id.messageEditText)
    private EditText messageEditText;

    private Handler handler;
    private ChatWindow chatWindow;
    private ChatDisposer disposer;
    private List<String> imageAttachments;
    private List<ChatMessage.ForwardedMessage> forwardedMessages;
    private long timestamp;
    private boolean keyboardIsOpen;

    public ChatWindowViewHandler(ChatWindow chatWindow, BaseWindow baseWindow) {
        super(baseWindow);
        this.chatWindow = chatWindow;
        this.imageAttachments = new ArrayList<>();
        this.forwardedMessages = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void handle(Context context) {}

    public void init(ChatDisposer chatDisposer) {
        this.disposer = chatDisposer;

        messagesListView.init(disposer, this, aVoid -> {
            if (!keyboardIsOpen) return;
            InputMethodManager imm = (InputMethodManager) context().getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getActivity().getActivity().getCurrentFocus();
            if (view == null) view = new View(context());
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        KeyboardVisibilityEvent.setEventListener(getActivity().getActivity(), isOpen -> {
            keyboardIsOpen = !keyboardIsOpen;
            messagesListView.scrollToBottom();
        });

        titleView.setText(disposer.getTitle());
        statusView.setText(disposer.getStatus());
        messageEditText.addTextChangedListener(new ComposeInteractor());

        Glide.with(context())
                .load(disposer.getPhotoUri())
                .centerCrop()
                .into(photoView);

        disposer.attachMessageObserver(new MessagesExtractor(null));
        disposer.sendActiveStatus();
    }

    public MessagesListView getMessagesListView() {
        return messagesListView;
    }

    public List<String> getImageAttachments() {
        return imageAttachments;
    }

    public void clearImageAttachments() {
        imageAttachments.clear();
    }

    public List<ChatMessage.ForwardedMessage> getForwardedMessages() {
        return forwardedMessages;
    }

    public void clearForwardedMessages() {
        forwardedMessages.clear();
    }

    public void clearAll() {
        messageEditText.getText().clear();
        clearImageAttachments();
        clearForwardedMessages();
    }

    @OnClick(R.id.sendMessageButton)
    public void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            chatWindow.sendMessage(message);
        }
    }

    @OnClick(R.id.pickPhotoButton)
    public void pickPhoto() {
        Toast.makeText(context(), "Пик фото", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messagesListView.onDestroy();
        handler = null;
    }

    @Override
    public void onDateUpdate(ChatMessage message) {
        if (timestamp == message.getTimestamp())
            return;

        timestamp = message.getTimestamp();
        handler.post(() -> dateTimeTextView.setText(Tools.getDateStamp(timestamp)));
    }

    @Override
    public void setDateVisibility(boolean bool) {
        handler.post(() -> {
            if (bool) {
                if (dateTimeView.getVisibility() == View.INVISIBLE) {
                    dateTimeView.startAnimation(Animations.slideUp());
                    dateTimeView.setVisibility(View.VISIBLE);
                }
            } else {
                if (dateTimeView.getVisibility() == View.VISIBLE) {
                    dateTimeView.startAnimation(Animations.slideDown());
                    dateTimeView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public class MessagesExtractor extends AbstractMessageObserver
    {
        public MessagesExtractor(EntityBareJid chatJid) {
            super(chatJid);
        }

        @Override
        public void messageReceived(ChatMessage message) throws InterruptedException {
            handler.post(() -> {
                messagesListView.addReceivedMessage(message);
                statusView.setText(disposer.getStatus());
            });
        }

        @Override
        public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
            handler.post(() -> {
                messagesListView.getAdapter().markAsRead();

                switch (status) {
                    case composing:
                        statusView.setText("печатают...");
                        handler.postDelayed(() -> statusView.setText(disposer.getStatus()), 6000);
                        break;
                }
            });
        }
    }

    public class ComposeInteractor extends TextWatcherAdapter
    {
        boolean wasChanged;

        @Override
        public void afterTextChanged(Editable s) {
            if (!wasChanged) {
                disposer.sendComposingStatus();
                wasChanged = true;
                handler.postDelayed(() -> wasChanged = false, 6000);
            }
        }
    }
}
