package org.hvkz.hvkz.modules.chats.chatsroster;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhargavms.dotloader.DotLoader;
import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.router.RouteChannel;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uimodels.ViewBinder;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

public class ContactViewHolder extends RecyclerView.ViewHolder
{
    private User user;

    @BindView(R.id.dialogItem)
    private LinearLayout contactLayout;

    @BindView(R.id.dialogAvatar)
    private CircleImageView photoView;

    @BindView(R.id.status_online)
    private View statusOnline;

    @BindView(R.id.typing_indicator)
    private DotLoader typingIndicator;

    @BindView(R.id.dialogName)
    private TextView nameView;

    @BindView(R.id.lastMessage)
    private TextView messageView;

    @BindView(R.id.timestamp)
    private TextView timeView;

    public ContactViewHolder(View v) {
        super(v);
        ViewBinder.handle(this, v);
    }

    public void hold(ContactsAdapter.Contact contact) {
        contact.setPosition(getAdapterPosition());

        user = contact.getUser();
        nameView.setText(contact.getUser().getShortName());

        ChatMessage message = contact.getLastMessage();

        if (message != null) {
            String text = "";
            if (message.isMine(contactLayout.getContext()))
                text += "Вы: ";
            text += message.getBody();

            if (message.getBody().isEmpty()) {
                text += "<вложение>";
            }

            messageView.setText(text);
            timeView.setVisibility(View.VISIBLE);
            timeView.setText(Tools.getTimeStringFormat(message.getTimestamp()));
        } else {
            messageView.setText("Здравствуйте! Я отвечу на любые Ваши вопросы.");
            timeView.setVisibility(View.GONE);
        }

        if (contact.isAvailable()) {
            statusOnline.setBackgroundResource(R.drawable.online);
        } else {
            statusOnline.setBackgroundResource(R.drawable.offline);
        }

        if (contact.isComposing()) {
            messageView.setText("печатает...");
            typingIndicator.setVisibility(View.VISIBLE);
        } else {
            typingIndicator.setVisibility(View.INVISIBLE);
        }

        Glide.with(context())
                .load(contact.getUser().getPhotoUrl())
                .centerCrop()
                .into(photoView);
    }

    public Context context() {
        return contactLayout.getContext();
    }

    @OnClick(R.id.dialogItem)
    public void onContactClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_TYPE_KEY, ChatType.PERSONAL_CHAT);
        bundle.putString(DOMAIN_KEY, String.valueOf(user.getUserId()));
        RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(bundle));
    }

    @OnLongClick(R.id.dialogItem)
    public void onContactLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context());
        builder.setTitle("Выберите действие")
                .setItems(new String[]{"Удалить диалог вместе с историей сообщений"}, (dialog, which) -> {
                    ContextApp.getApp(context()).getMessagesStorage()
                            .clearHistory(String.valueOf(user.getUserId()));

                    Toast.makeText(context(), "История очищена", Toast.LENGTH_SHORT).show();
                });

        builder.create().show();
    }
}
