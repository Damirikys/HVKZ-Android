package org.hvkz.hvkz.modules.chats.window;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.users.UsersDb;
import org.hvkz.hvkz.models.ViewBinder;
import org.hvkz.hvkz.modules.gallery.ImagesProvider;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.models.ChatMessage;

import java.util.List;

import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final int IMG_SIZE = dpToPx(300);
    private static final int IMG_MARGIN = dpToPx(2);
    private static final int MINE_COLOR = Color.parseColor("#fffcd7");
    private static final int SELECTED_COLOR = Color.LTGRAY;

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

    public MessageViewHolder(View itemView, MessagesSelector selector) {
        super(itemView);
        this.selector = selector;
        ViewBinder.handle(this, itemView);
    }

    public boolean isMine() {
        return message.isMine();
    }

    public void bindMessage(ChatMessage _message) {
        this.message = _message;

        messageText.setText(message.getBody());
        timeView.setText(Tools.getTimeFromUnix(message.getTimestamp()));
        messageTape.setBackgroundColor(Color.argb(0, 0, 0, 0));
        messageTape.setOnLongClickListener(this);
        messageTape.setOnClickListener(this);

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

                UsersDb.getById(fm.getSender(), user -> {
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
        bubbleContainer.setGravity(Gravity.END);
        photo.setVisibility(View.INVISIBLE);
        timeView.setVisibility(View.VISIBLE);

        if (!message.isRead() && beforeIsMine)
            messageTape.setBackgroundColor(Color.argb(10, 0, 0, 0));
    }

    public void setupNotMineDisplay(ChatMessage beforeMessage) {
        timeView.setVisibility(View.INVISIBLE);
        bubbleContainer.setGravity(Gravity.START);

        if (beforeMessage == null || beforeMessage.getSenderId() != message.getSenderId()) {
            photo.setVisibility(View.VISIBLE);

            UsersDb.getById(message.getSenderId(), user -> Glide.with(photo.getContext())
                    .load(user.getPhotoUrl())
                    .fitCenter()
                    .centerCrop()
                    .into(photo));
        } else {
            photo.setVisibility(View.INVISIBLE);
            timeView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        selector.onMessageClick(message, wasAdded -> {
            if (wasAdded) {
                bubbleLayout.setCardBackgroundColor(SELECTED_COLOR);
            } else {
                bubbleLayout.setCardBackgroundColor((isMine()) ? MINE_COLOR : Color.WHITE);
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        if (!message.getBody().isEmpty()) {
            selector.setSelectorEnable(true);
            onClick(v);
        }

        return true;
    }
}