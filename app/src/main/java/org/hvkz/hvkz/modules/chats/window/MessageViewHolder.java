package org.hvkz.hvkz.modules.chats.window;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.firebase.db.users.UsersStorage;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.models.ViewBinder;
import org.hvkz.hvkz.modules.RouteChannel;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.modules.gallery.ImagesProvider;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.models.ChatMessage;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;
import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class MessageViewHolder extends RecyclerView.ViewHolder
{
    private static Integer IMG_SIZE;
    private static Integer IMG_MARGIN;
    private static final int MINE_COLOR = Color.parseColor("#fffcd7");
    private static final int SELECTED_COLOR = Color.LTGRAY;

    private MessagesListAdapter adapter;
    private PopupMenu popupMenu;
    private UsersStorage usersStorage;
    private MessagesSelector selector;
    private ChatMessage message;

    @BindView(R.id.message_tape)
    private LinearLayout messageTape;

    @BindView(R.id.forwardedLayout)
    private LinearLayout forwardedLayout;

    @BindView(R.id.gridImageLayout)
    private GridLayout gridLayout;

    @BindView(R.id.bubbleContainer)
    private LinearLayout bubbleContainer;

    @BindView(R.id.chatAvatar)
    private ImageView photo;

    @BindView(R.id.bubble_layout)
    private CardView bubbleLayout;

    @BindView(R.id.message_text)
    private TextView messageText;

    @BindView(R.id.chatHideTime)
    private TextView timeView;

    public MessageViewHolder(View itemView, MessagesSelector selector, MessagesListAdapter adapter) {
        super(itemView);
        ViewBinder.handle(this, itemView);

        if (IMG_SIZE == null) {
            IMG_SIZE = dpToPx(itemView.getResources().getDisplayMetrics(), 300);
            IMG_MARGIN = dpToPx(itemView.getResources().getDisplayMetrics(), 2);
        }

        this.adapter = adapter;
        this.popupMenu = new PopupMenu(context(), itemView);
        this.usersStorage = ContextApp.getApp(itemView.getContext()).getUsersStorage();
        this.selector = selector;
    }

    public boolean isMine() {
        return message.isMine(messageTape.getContext());
    }

    public void bindMessage(ChatMessage _message) {
        this.message = _message;

        messageText.setVisibility(View.VISIBLE);
        messageText.setText(message.getBody());
        timeView.setText(Tools.getTimeFromUnix(message.getTimestamp()));
        messageTape.setBackgroundColor(Color.argb(0, 0, 0, 0));

        layoutImages();
        layoutForwardedMessages();

        if (selector.getSelectedMessages().contains(message.toForward())) {
            bubbleLayout.setCardBackgroundColor(SELECTED_COLOR);
        } else {
            bubbleLayout.setCardBackgroundColor((isMine()) ? MINE_COLOR : Color.WHITE);
        }
    }

    private void layoutImages() {
        List<String> images = message.getImages();
        if (images.size() > 0) {
            gridLayout.removeAllViews();

            int imagesCount = images.size();
            int mod = imagesCount % 2;
            int colCount = (mod == 0) ? 2 : 1;

            if (message.getBody().isEmpty()) {
                messageText.setVisibility(View.GONE);
            }

            gridLayout.setColumnCount(colCount);

            for (int i = 0; i < imagesCount; i++) {
                final ImageView imageView = new ImageView(gridLayout.getContext());
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(IMG_SIZE / colCount, IMG_SIZE / colCount);
                params.setMargins(IMG_MARGIN, IMG_MARGIN, IMG_MARGIN, IMG_MARGIN);
                imageView.setLayoutParams(params);
                imageView.setBackgroundColor(Color.WHITE);
                imageView.setOnClickListener(new ImagesProvider(images, i));

                Glide.with(gridLayout.getContext())
                        .load(images.get(i))
                        .centerCrop()
                        .placeholder(R.drawable.imgplaceholder)
                        .into(imageView);

                gridLayout.addView(imageView);
            }

            gridLayout.setVisibility(View.VISIBLE);
        } else {
            gridLayout.setVisibility(View.GONE);
            messageText.setVisibility(View.VISIBLE);
        }
    }

    private void layoutForwardedMessages() {
        if (message.getForwarded() != null) {
            forwardedLayout.removeAllViews();

            if (message.getBody().isEmpty()) {
                messageText.setVisibility(View.GONE);
            }

            for (ChatMessage.Forwarded fm: message.getForwarded()) {
                View forwardedView = LayoutInflater.from(forwardedLayout.getContext())
                        .inflate(R.layout.forwarded_message, forwardedLayout, false);

                TextView name = (TextView) forwardedView.findViewById(R.id.forwardedName);
                TextView date = (TextView) forwardedView.findViewById(R.id.forwardedDate);
                TextView body = (TextView) forwardedView.findViewById(R.id.forwardedBody);
                ImageView avatar = (ImageView) forwardedView.findViewById(R.id.forwardedAvatar);

                date.setText(Tools.getDateStamp(fm.getTimestamp()));
                body.setText(fm.getMessage());

                usersStorage.getByIdFromCache(fm.getSender(), user -> {
                    name.setText(user.getShortName());

                    Glide.with(forwardedView.getContext())
                            .load(user.getPhotoUrl())
                            .centerCrop()
                            .into(avatar);
                });

                forwardedLayout.addView(forwardedView);
            }

            forwardedLayout.setVisibility(View.VISIBLE);
        } else {
            forwardedLayout.setVisibility(View.GONE);
            messageText.setVisibility(View.VISIBLE);
        }
    }

    public void setupMineDisplay(boolean beforeIsMine) {
        popupMenu.setGravity(Gravity.RIGHT);
        bubbleContainer.setGravity(Gravity.END);
        photo.setVisibility(View.INVISIBLE);
        timeView.setVisibility(View.VISIBLE);

        if (!message.isRead() && beforeIsMine)
            messageTape.setBackgroundColor(Color.argb(10, 0, 0, 0));
    }

    public void setupNotMineDisplay(ChatMessage beforeMessage) {
        popupMenu.setGravity(Gravity.LEFT);
        timeView.setVisibility(View.INVISIBLE);
        bubbleContainer.setGravity(Gravity.START);

        if (beforeMessage == null || beforeMessage.getSenderId() != message.getSenderId()) {
            photo.setVisibility(View.VISIBLE);
                   usersStorage.getByIdFromCache(message.getSenderId(), user -> Glide.with(photo.getContext())
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .into(photo));
        } else {
            photo.setVisibility(View.INVISIBLE);
            timeView.setVisibility(View.VISIBLE);
        }
    }

    public Context context() {
        return messageTape.getContext();
    }

    @OnClick(R.id.chatAvatar)
    public void onUserCardOpen(View view) {
        usersStorage.getByIdFromCache(message.getSenderId(),
                user -> {
                    final AlertDialog dialog = new AlertDialog.Builder(context(), R.style.MyDialogTheme)
                            .setCancelable(true)
                            .create();

                    dialog.setView(CardWrapper.inflate(context(), user, avoid -> dialog.dismiss()));
                    dialog.show();
                });
    }

    @OnClick(R.id.message_tape)
    public void onMessageClick(View view) {
        if (!selector.isEnable()) {
            Menu menu = popupMenu.getMenu();
            menu.clear();

            MenuItem copyItem = menu.add("Копировать");
            copyItem.setOnMenuItemClickListener(item -> {
                ClipboardManager manager = (ClipboardManager) context().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", message.getBody());
                manager.setPrimaryClip(clip);

                Toast.makeText(context(), "Скопировано", Toast.LENGTH_SHORT).show();
                return false;
            });

            MenuItem deleteItem = menu.add("Удалить");
            deleteItem.setOnMenuItemClickListener(item -> {
                adapter.remove(message);
                return false;
            });

            popupMenu.show();
        } else {
            selector.onMessageClick(message, wasAdded -> {
                if (wasAdded) {
                    bubbleLayout.setCardBackgroundColor(SELECTED_COLOR);
                } else {
                    bubbleLayout.setCardBackgroundColor((isMine()) ? MINE_COLOR : Color.WHITE);
                }
            });
        }
    }

    @OnLongClick(R.id.message_tape)
    public boolean onMessageLongClick(View view) {
        if (!message.getBody().isEmpty()) {
            selector.setSelectorEnable(true);
            onMessageClick(view);
        }

        return true;
    }

    private static class CardWrapper
    {
        private View view;
        private User user;
        private Callback<Void> dismiss;

        @BindView(R.id.photo)
        private CircleImageView photoView;

        @BindView(R.id.full_name)
        private TextView nameView;

        @BindView(R.id.email)
        private TextView emailView;

        private CardWrapper(View view, User user, Callback<Void> callback) {
            this.view = view;
            this.user = user;
            this.dismiss = callback;
            ViewBinder.handle(this, view);

            this.nameView.setText(user.getDisplayName());
            this.emailView.setText(user.getEmail());

            Glide.with(view.getContext())
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .into(photoView);
        }

        private View getView() {
            return view;
        }

        @OnClick(R.id.sendMessageButton)
        public void onSendMessageClick(View view) {
            String localpart = String.valueOf(user.getUserId());

            if (!ContextApp.getApp(view.getContext()).getNotificationService().isLocked(localpart)) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CHAT_TYPE_KEY, ChatType.PERSONAL_CHAT);
                bundle.putString(DOMAIN_KEY, String.valueOf(user.getUserId()));
                RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(bundle));
            }

            dismiss.call(null);
        }

        private static View inflate(Context context, User user, Callback<Void> dismiss) {
            return new CardWrapper(LayoutInflater.from(context)
                    .inflate(R.layout.user_card_layout, null), user, dismiss)
                    .getView();
        }
    }
}