package org.hvkz.hvkz.modules.chats.window;

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

import com.bumptech.glide.Glide;
import com.eralp.circleprogressview.CircleProgressView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.utils.Animations;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.message_service.AbstractMessageObserver;
import org.hvkz.hvkz.xmpp.models.ChatMessage;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;

import java.util.ArrayList;
import java.util.List;

import static org.hvkz.hvkz.modules.chats.window.ChatWindowFragment.GALLERY_REQUEST;

public class ChatWindowViewHandler extends ViewHandler implements DateChangeListener, MessagesSelector.OnSelectMessageListener
{
    @BindView(R.id.buddy_avatar)
    private ImageView photoView;

    @BindView(R.id.buddy_name)
    private TextView titleView;

    @BindView(R.id.buddy_status)
    private TextView statusView;

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
    private List<ChatMessage.Forwarded> forwardedMessages;

    private PhotoAttachmentsExecutor photoAttachmentsExecutor;
    private long timestamp;
    private boolean keyboardIsOpen;

    public ChatWindowViewHandler(ChatWindow chatWindow, BaseWindow baseWindow) {
        super(baseWindow);
        this.chatWindow = chatWindow;
        this.forwardedMessages = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.photoAttachmentsExecutor = new PhotoAttachmentsExecutor();
    }

    @Override
    protected void handle(Context context) {}

    public void init(ChatDisposer chatDisposer) {
        this.disposer = chatDisposer;

        messagesListView.init(disposer, this, aVoid -> {
            if (!keyboardIsOpen) return;
            InputMethodManager imm = (InputMethodManager) context().getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = getWindow().getActivity().getCurrentFocus();
            if (view == null) view = new View(context());
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

        messagesListView.getAdapter().setOnSelectMessageListener(this);

        KeyboardVisibilityEvent.setEventListener(getWindow().getActivity(), isOpen -> {
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

    public void setAttachmentsVisible(boolean bool) {
        if (bool) {
            attachmentsView.setVisibility(View.VISIBLE);
            if (!messagesListView.isScrolling()) messagesListView.scrollToBottom();
        } else {
            attachmentsView.setVisibility(View.GONE);
        }
    }

    public void attachPhoto(Uri uri) {
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

        if (photoAttachmentsExecutor.getUploadCounter() == 0) {
            Toast.makeText(context(), "Пожалуйста, дождитесь загрузки фотографии", Toast.LENGTH_SHORT).show();
        }

        photoAttachmentsExecutor.upload(uri, progressView, imageView, new Callback<Uri>() {
            @Override
            public void call(Uri url) {
                layout.animate().scaleY(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        imageAttachView.removeView(layout);
                        photoAttachmentsExecutor.delete(url);
                        if (imageAttachView.getChildCount() == 0) {
                            imageAttachView.setVisibility(View.GONE);
                            setAttachmentsVisible(false);
                        }
                    }
                });
            }
        });
    }

    public MessagesListView getMessagesListView() {
        return messagesListView;
    }

    public List<Uri> getImageAttachments() {
        return photoAttachmentsExecutor.getAttachedPhotos();
    }

    public List<ChatMessage.Forwarded> getForwardedMessages() {
        return forwardedMessages;
    }

    public void clearAll() {
        messageEditText.getText().clear();
        photoAttachmentsExecutor.clear();
        forwardedMessages.clear();
        imageAttachView.removeAllViews();
        forwardedTextAttach.setVisibility(View.GONE);
        setAttachmentsVisible(false);
        messagesListView.getAdapter().notifyDataSetChanged();
    }

    @OnClick(R.id.sendMessageLayout)
    public void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty() || photoAttachmentsExecutor.getAttachedPhotos().size() > 0 || forwardedMessages.size() > 0) {
            chatWindow.sendMessage(message);
        }
    }

    @OnClick(R.id.pickPhotoLayout)
    public void pickPhoto() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity().startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    @Override
    public void onSelect(List<ChatMessage.Forwarded> selected) {
        if (!selected.isEmpty()) {
            setAttachmentsVisible(true);
            forwardedTextAttach.setVisibility(View.VISIBLE);
            forwardedTextAttach.setText("В ответ на " + selected.size() + " "
                    + Tools.padezh("сообщени", "е", "я", "й", selected.size()));

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        messagesListView.onDestroy();
        photoAttachmentsExecutor.onDestroy();
        photoAttachmentsExecutor = null;
        handler = null;
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
