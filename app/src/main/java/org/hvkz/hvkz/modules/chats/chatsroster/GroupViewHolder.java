package org.hvkz.hvkz.modules.chats.chatsroster;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bhargavms.dotloader.DotLoader;
import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.router.RouteChannel;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uimodels.ViewBinder;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.xmpp.messaging.ChatMessage;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

public class GroupViewHolder extends RecyclerView.ViewHolder
{
    private Group group;

    private View view;

    @BindView(R.id.lastMessageLayout)
    private View lastMessageLayout;

    @BindView(R.id.adminPhoto)
    private ImageView adminPhotoView;

    @BindView(R.id.adminName)
    private TextView adminNameView;

    @BindView(R.id.membersInfo)
    private TextView membersView;

    @BindView(R.id.notice)
    private TextView noticeView;

    @BindView(R.id.typing_indicator)
    private DotLoader typingIndicator;

    @BindView(R.id.lastMessageName)
    private TextView lastMessageName;

    @BindView(R.id.photo_last)
    private CircleImageView memberPhotoView;

    @BindView(R.id.text_last)
    private TextView messageView;

    @BindView(R.id.time_last)
    private TextView timeView;

    public GroupViewHolder(View itemView) {
        super(itemView);
        this.view = itemView;
        ViewBinder.handle(this, itemView);
    }

    public void bind(GroupsPagerAdapter.GroupItem groupItem) {
        this.group = groupItem.getGroup();
        this.adminNameView.setText(group.getAdmin().getDisplayName());
        this.noticeView.setText(group.getNotice());
        this.membersView.setText(group.getMembers().size() + " " + Tools.declension("участник", "", "а", "ов", group.getMembers().size()));

        Glide.with(context())
                .load(group.getAdmin().getPhotoUrl())
                .centerCrop()
                .into(adminPhotoView);

        if (groupItem.isComposing()) {
            typingIndicator.setVisibility(View.VISIBLE);
        } else {
            typingIndicator.setVisibility(View.INVISIBLE);
        }

        ChatMessage message = groupItem.getLastMessage();
        if (message != null) {
            User user = group.getMembers().get(message.getSenderId());

            lastMessageLayout.setVisibility(View.VISIBLE);
            lastMessageName.setText(user.getShortName());

            Glide.with(context())
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .into(memberPhotoView);

            String text = "";
            if (message.isMine(context()))
                text += "Вы: ";
            text += message.getBody();

            if (message.getBody().isEmpty()) {
                text += "<вложение>";
            }

            messageView.setText(text);
            timeView.setText(Tools.getTimeStringFormat(message.getTimestamp()));
        } else {
            lastMessageLayout.setVisibility(View.GONE);
        }
    }

    public View getView() {
        return view;
    }

    @OnClick(R.id.groupCardView)
    public void onGroupClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_TYPE_KEY, ChatType.MULTI_USER_CHAT);
        bundle.putString(DOMAIN_KEY, group.getGroupName());
        RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(bundle));
    }

    public Context context() {
        return adminNameView.getContext();
    }
}