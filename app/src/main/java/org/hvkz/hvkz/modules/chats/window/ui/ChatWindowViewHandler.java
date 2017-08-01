package org.hvkz.hvkz.modules.chats.window.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.bumptech.glide.Glide;
import com.eralp.circleprogressview.CircleProgressView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.EventReceiver;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.event.Event;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.modules.chats.window.ChatWindow;
import org.hvkz.hvkz.modules.chats.window.messages.MessagesListView;
import org.hvkz.hvkz.modules.chats.window.tools.MessagesSelector;
import org.hvkz.hvkz.modules.chats.window.tools.PhotoAttachmentsExecutor;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.utils.Animations;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.messaging.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowViewHandler extends ViewHandler<ChatWindowPresenter> implements
        OnDateChangeListener, MessagesSelector.OnSelectMessageListener
{
    @BindView(R.id.photo_toolbar)
    private CircleImageView photoView;

    @BindView(R.id.status_online)
    private View statusOnline;

    @BindView(R.id.title_toolbar)
    private TextView titleView;

    @BindView(R.id.subtitle_toolbar)
    private TextView statusView;

    @BindView(R.id.typing_indicator)
    private DotLoader typingIndicator;

    @BindView(R.id.attachments)
    private HorizontalScrollView attachmentsView;

    @BindView(R.id.forwardedInfo)
    private TextView forwardedTextAttach;

    @BindView(R.id.img_attach)
    private LinearLayout imageAttachView;

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
    private ComposeInteractor composeInteractor;
    private List<ChatMessage.Forwarded> forwardedMessages;

    private PhotoAttachmentsExecutor imagesAttachExectutor;
    private long timestamp;
    private boolean keyboardIsOpen;

    ChatWindowViewHandler(ChatWindow chatWindow, BaseWindow<ChatWindowPresenter> baseWindow) {
        super(baseWindow);
        this.chatWindow = chatWindow;
        this.forwardedMessages = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.imagesAttachExectutor = new PhotoAttachmentsExecutor();
        EventChannel.connect(this);
    }

    @Override
    protected void handle(Context context) {}

    public void init(ChatDisposer chatDisposer) {
        if (disposer == null) {
            chatDisposer.sendActiveStatus();

            messagesListView.init(this, aVoid -> {
                if (!keyboardIsOpen) return;
                InputMethodManager imm = (InputMethodManager) context().getSystemService(Activity.INPUT_METHOD_SERVICE);
                View view = activity().getCurrentFocus();
                if (view == null) view = new View(context());
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            });


            messageEditText.addTextChangedListener((composeInteractor == null)
                    ? composeInteractor = new ComposeInteractor()
                    : composeInteractor
            );

            KeyboardVisibilityEvent.setEventListener(activity(), isOpen -> {
                keyboardIsOpen = !keyboardIsOpen;
                messagesListView.scrollToBottom();
            });
        }

        disposer = chatDisposer;
        disposer.attachMessageObserver(new MessagesExtractor(null));

        Roster roster = disposer.getService().getRoster();
        RosterEntry rosterEntry = roster.getEntry(chatDisposer.getChatJid().asBareJid());
        if (rosterEntry == null || !rosterEntry.canSeeHisPresence()) {
            try {
                roster.sendSubscriptionRequest(chatDisposer.getChatJid().asBareJid());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (disposer.getMessageType() == Message.Type.chat) {
            if (roster.getPresence(chatDisposer.getChatJid().asBareJid()).isAvailable()) {
                statusOnline.setBackgroundResource(R.drawable.online);
            } else {
                statusOnline.setBackgroundResource(R.drawable.offline);
            }
        } else {
            statusOnline.setVisibility(View.GONE);
        }

        statusView.setText(chatDisposer.getDefaultStatus());
        titleView.setText(disposer.getTitle());
        titleView.postDelayed(() -> {
            if (!titleView.isSelected()) {
                titleView.setSelected(true);
            }
        }, 3000);

        Glide.with(context())
                .load(disposer.getPhotoUri())
                .centerCrop()
                .into(photoView);
    }

    private void setAttachmentsVisible(boolean bool) {
        if (bool) {
            attachmentsView.setVisibility(View.VISIBLE);
            if (!messagesListView.isScrolling()) messagesListView.scrollToBottom();
        } else {
            attachmentsView.setVisibility(View.GONE);
        }
    }

    void attachPhoto(Uri uri) {
        setAttachmentsVisible(true);
        imageAttachView.setVisibility(View.VISIBLE);

        final RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context())
                .inflate(R.layout.image_attachment, null);
        final ImageView imageView = (ImageView) layout.getChildAt(0);
        final CircleProgressView progressView = (CircleProgressView) layout.findViewById(R.id.circle_progress_view);

        Glide.with(layout.getContext())
                .load(uri)
                .centerCrop()
                .into(imageView);

        imageAttachView.addView(layout);

        if (imagesAttachExectutor.getUploadCounter() == 0) {
            Toast.makeText(context(), R.string.please_wait_photo_uploading, Toast.LENGTH_SHORT).show();
        }

        imagesAttachExectutor.upload(uri, progressView, imageView, new Callback<Uri>() {
            @Override
            public void call(Uri url) {
                layout.animate().scaleY(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        imageAttachView.removeView(layout);
                        imagesAttachExectutor.delete(url);
                        if (imageAttachView.getChildCount() == 0) {
                            imageAttachView.setVisibility(View.GONE);
                            setAttachmentsVisible(false);
                        }
                    }
                });
            }
        });
    }

    MessagesListView getMessagesListView() {
        return messagesListView;
    }

    List<Uri> getImageAttachments() {
        return imagesAttachExectutor.getAttachedPhotos();
    }

    List<ChatMessage.Forwarded> getForwardedMessages() {
        return forwardedMessages;
    }

    void clearAll() {
        messageEditText.getText().clear();
        imagesAttachExectutor.clear();
        forwardedMessages.clear();
        imageAttachView.removeAllViews();
        imageAttachView.setVisibility(View.GONE);
        forwardedTextAttach.setVisibility(View.GONE);
        setAttachmentsVisible(false);
        messagesListView.getAdapter().notifyDataSetChanged();
    }

    @OnClick(R.id.sendMessageLayout)
    public void onSendButtonClick(View view) {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty() || imagesAttachExectutor.getAttachedPhotos().size() > 0 || forwardedMessages.size() > 0)
            chatWindow.sendMessage(message);
    }

    @OnClick(R.id.pickPhotoLayout)
    public void onPhotoAttachClick(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity().startActivityForResult(photoPickerIntent, ChatWindowFragment.GALLERY_REQUEST);
    }

    @OnClick(R.id.toolbar_layout)
    public void onToolbarClick(View view) {
        disposer.onToolbarClick(context());
    }

    @Override
    public void onSelect(List<ChatMessage.Forwarded> selected) {
        if (!selected.isEmpty()) {
            setAttachmentsVisible(true);
            forwardedTextAttach.setVisibility(View.VISIBLE);
            forwardedTextAttach.setText(string(R.string.reply_to) + " " + selected.size() + " "
                    + Tools.declension("сообщени", "е", "я", "й", selected.size()));
            forwardedMessages = selected;
        } else {
            forwardedTextAttach.setVisibility(View.GONE);
            if (imageAttachView.getChildCount() == 0)
                setAttachmentsVisible(false);
        }
    }

    @Override
    public void onDateUpdate(ChatMessage message) {
        if (timestamp == message.getTimestamp())
            return;

        timestamp = message.getTimestamp();
        handler.post(() -> dateTimeTextView.setText(Tools.getDateStringFormat(timestamp)));
    }

    @Override
    public void setDateVisibility(boolean bool) {
        handler.post(() -> {
            if (bool) {
                if (dateTimeView.getVisibility() == View.INVISIBLE) {
                    dateTimeView.startAnimation(Animations.slideUp(context()));
                    dateTimeView.setVisibility(View.VISIBLE);
                }
            } else {
                if (dateTimeView.getVisibility() == View.VISIBLE) {
                    dateTimeView.startAnimation(Animations.slideDown(context()));
                    dateTimeView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @EventReceiver
    public void onUpdateEventReceive(Event<Object> event) {
        if (event.getType() == Event.EventType.UPDATE_GROUP_CHAT_WINDOW) {
            this.titleView.setText(disposer.getTitle());
            this.statusView.setText(disposer.getDefaultStatus());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventChannel.disconnect(this);
        disposer.sendInactiveStatus();
        disposer.onDestroy();
        messagesListView.onDestroy();
        imagesAttachExectutor.onDestroy();
        imagesAttachExectutor = null;
        handler = null;
    }

    private class MessagesExtractor extends AbstractMessageObserver
    {
        MessagesExtractor(EntityBareJid chatJid) {
            super(chatJid);
        }

        @Override
        public void messageReceived(ChatMessage message) throws InterruptedException {
            handler.post(() -> {
                messagesListView.addReceivedMessage(message);
                statusView.setText(disposer.getActiveStatus());
                typingIndicator.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void statusReceived(ChatState status, BareJid userJid) throws InterruptedException {
            handler.post(() -> {
                messagesListView.getAdapter().markAsRead();
                switch (status) {
                    case active:
                        statusView.setText(disposer.getActiveStatus());
                        break;
                    case inactive:
                        statusView.setText(disposer.getDefaultStatus());
                        break;
                    case composing:
                        statusView.setText(disposer.getActiveStatus());
                        typingIndicator.setVisibility(View.VISIBLE);
                        handler.postDelayed(() -> typingIndicator.setVisibility(View.INVISIBLE), 6000);
                        break;
                }
            });
        }

        @Override
        public void presenceReceived(Presence.Type type) {
            handler.post(() -> {
                switch (type) {
                    case available:
                        statusView.setText(R.string.online);
                        statusOnline.setBackgroundResource(R.drawable.online);
                        break;
                    case unavailable:
                        statusView.setText(R.string.offline);
                        statusOnline.setBackgroundResource(R.drawable.offline);
                        break;
                }
            });
        }
    }

    private class ComposeInteractor extends TextWatcherAdapter
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
