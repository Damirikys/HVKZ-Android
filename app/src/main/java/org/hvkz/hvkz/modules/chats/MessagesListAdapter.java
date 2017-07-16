package org.hvkz.hvkz.modules.chats;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.users.UsersDb;
import org.hvkz.hvkz.models.ViewBinder;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.modules.gallery.ImagesProvider;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder>
{
    private static final int DP = dpToPx(7);
    private static final int IMG_SIZE = dpToPx(300);
    private static final int IMG_MARGIN = dpToPx(2);
    private static final int MINE_COLOR = Color.parseColor("#fffcd7");
    private static final int SELECTED_COLOR = Color.LTGRAY;

    private List<ChatMessage> data;
    private ChatDisposer disposer;

    public MessagesListAdapter(ChatDisposer disposer) {
        this.data = new ArrayList<>();
        this.disposer = disposer;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatbubble, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatMessage currentMessage = data.get(position);
        ChatMessage beforeMessage = (position == 0)
                ? null
                : data.get(position - 1);

        holder.bindMessage(currentMessage);
        displayProcessing(holder, currentMessage, beforeMessage);
        layoutImages(holder, currentMessage);
        layoutForwardedMessages(holder, currentMessage);
    }

    private void displayProcessing(ViewHolder holder, ChatMessage current, ChatMessage before) {
        if (holder.isMine) {
            setupMineDisplay(holder, current);
        } else {
            setupNotMineDisplay(holder, current, before);
        }
    }

    private void setupMineDisplay(ViewHolder holder, ChatMessage message) {
        holder.bubbleLayout.setCardBackgroundColor(MINE_COLOR);
        holder.bubbleContainer.setGravity(Gravity.END);
        holder.photo.setVisibility(View.INVISIBLE);
        holder.timeView.setVisibility(View.VISIBLE);

        if (!message.isRead() && data.get(data.size() - 1).isMine())
            holder.messageTape.setBackgroundColor(Color.argb(10, 0, 0, 0));
    }

    private void setupNotMineDisplay(ViewHolder holder, ChatMessage message, ChatMessage beforeMessage) {
        holder.timeView.setVisibility(View.INVISIBLE);
        holder.bubbleLayout.setCardBackgroundColor(Color.WHITE);
        holder.bubbleContainer.setGravity(Gravity.START);

        if (beforeMessage == null || beforeMessage.getSenderId() != message.getSenderId()) {
            holder.photo.setVisibility(View.VISIBLE);

            UsersDb.getById(message.getSenderId(), user -> Glide.with(holder.photo.getContext())
                    .load(user.getPhotoUrl())
                    .fitCenter()
                    .centerCrop()
                    .into(holder.photo));
        } else {
            holder.photo.setVisibility(View.INVISIBLE);
            holder.timeView.setVisibility(View.VISIBLE);
        }
    }

    private void layoutImages(ViewHolder holder, ChatMessage message) {
        List<String> images = message.getImages();
        if (images != null && holder.gridLayout.getChildCount() == 0) {
            if (message.getBody().isEmpty())
                holder.contentBody.removeView(holder.messageText);

            int imagesCount = images.size();
            int mod = imagesCount % 2;
            int colCount = (mod == 0) ? 2 : 1;

            holder.gridLayout.setColumnCount(colCount);

            for (int i = 0; i < imagesCount; i++) {
                final ImageView imageView = new ImageView(holder.gridLayout.getContext());
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(IMG_SIZE / colCount, IMG_SIZE / colCount);
                params.setMargins(IMG_MARGIN, IMG_MARGIN, IMG_MARGIN, IMG_MARGIN);
                imageView.setLayoutParams(params);
                imageView.setBackgroundColor(Color.WHITE);
                imageView.setOnClickListener(new ImagesProvider(images, i));

                holder.gridLayout.addView(imageView);

                Glide.with(holder.gridLayout.getContext())
                        .load(images.get(i))
                        .centerCrop()
                        .placeholder(R.drawable.imgplaceholder)
                        .into(imageView);
            }
        }
    }

    private void layoutForwardedMessages(ViewHolder holder, ChatMessage message) {
        if (message.getForwarded() != null && holder.contentBody.findViewById(R.id.forwardedName) == null) {
            for (ChatMessage.ForwardedMessage fm: message.getForwarded()) {
                View forwardedView = LayoutInflater.from(holder.contentBody.getContext())
                        .inflate(R.layout.forwarded_message, holder.contentBody, false);

                TextView name = (TextView) forwardedView.findViewById(R.id.forwardedName);
                TextView date = (TextView) forwardedView.findViewById(R.id.forwardedDate);
                TextView body = (TextView) forwardedView.findViewById(R.id.forwardedBody);
                CircleImageView avatar = (CircleImageView) forwardedView.findViewById(R.id.forwardedAvatar);

                date.setText(Tools.getDateStamp(fm.getTimestamp()));
                body.setText(fm.getMessage());

                UsersDb.getById(fm.getSender(), user -> {
                    name.setText(user.getShortName());
                    Glide.with(holder.contentBody.getContext())
                            .load(user.getPhotoUrl())
                            .centerCrop()
                            .into(avatar);
                });

                holder.contentBody.addView(forwardedView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ChatMessage getItem(int position) {
        return data.get(position);
    }

    private void addAll(List<ChatMessage> messages) {
        data.addAll(0, messages);
        notifyItemRangeInserted(0, messages.size());
    }

    public List<ChatMessage> loadMore(int limit, int offset) {
        List<ChatMessage> messages = disposer.loadMore(limit, offset);
        addAll(messages);
        return messages;
    }

    public void addMyMessage(ChatMessage message) {
        addMessage(message);
        disposer.add(message);
    }

    public void addMessage(ChatMessage message) {
        data.add(message);
        notifyItemRangeChanged(data.size() - 2, 2);
    }

    public void removeAll(List<ChatMessage> messages) {
        data.removeAll(messages);
        notifyDataSetChanged();

        disposer.removeAll(messages);
    }

    public void markAsRead() {
        Log.d("MessagesListAdapter", "markAsRead");

        for(ChatMessage m: data)
            m.setRead(true);

        notifyDataSetChanged();
        disposer.markAsRead();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.message_tape)
        LinearLayout messageTape;

        @BindView(R.id.contentbody)
        LinearLayout contentBody;

        @BindView(R.id.gridImageLayout)
        GridLayout gridLayout;

        @BindView(R.id.bubbleContainer)
        LinearLayout bubbleContainer;

        @BindView(R.id.chatAvatar)
        ImageView photo;

        @BindView(R.id.bubble_layout)
        CardView bubbleLayout;

        @BindView(R.id.message_text)
        TextView messageText;

        @BindView(R.id.chatHideTime)
        TextView timeView;

        boolean isMine;

        ViewHolder(View itemView) {
            super(itemView);
            ViewBinder.handle(this, itemView);
        }

        public void bindMessage(ChatMessage message) {
            messageText.setText(message.getBody());
            timeView.setText(Tools.getTimeFromUnix(message.getTimestamp()));
            messageTape.setBackgroundColor(Color.argb(0, 0, 0, 0));
            isMine = message.isMine();
        }
    }
}
