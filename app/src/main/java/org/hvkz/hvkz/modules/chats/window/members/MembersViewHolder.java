package org.hvkz.hvkz.modules.chats.window.members;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.router.RouteChannel;
import org.hvkz.hvkz.templates.UniversalAdapter;
import org.hvkz.hvkz.templates.UniversalViewHolder;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.utils.JidFactory;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

class MembersViewHolder extends UniversalViewHolder<User>
{
    private MembersListAction actionListener;

    @BindView(R.id.dialogAvatar)
    private CircleImageView photoView;

    @BindView(R.id.dialogName)
    private TextView nameView;

    @BindView(R.id.lastMessage)
    private TextView emailView;

    @BindView(R.id.status_online)
    private View statusView;

    private MembersViewHolder(MembersListAction actionListener, View itemView) {
        super(itemView);
        this.actionListener = actionListener;
    }

    @Override
    public void hold() {
        this.nameView.setText(item().getDisplayName());
        this.emailView.setText(item().getEmail());

        Glide.with(context())
                .load(item().getPhotoUrl())
                .centerCrop()
                .into(photoView);

        ContextApp.getApp(context()).bindConnectionService(service -> {
            if(service.getRoster().getPresence(JidFactory.from(item())).isAvailable()) {
                statusView.setBackgroundResource(R.drawable.online);
            } else {
                statusView.setBackgroundResource(R.drawable.offline);
            }
        });
    }

    @OnClick(R.id.dialogItem)
    public void onMemberClick(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_TYPE_KEY, ChatType.PERSONAL_CHAT);
        bundle.putString(DOMAIN_KEY, String.valueOf(item().getUserId()));
        RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(bundle));

        actionListener.dismiss();
    }

    @OnLongClick(R.id.dialogItem)
    public void onMemberLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context());
        builder.setTitle(R.string.select_action).setItems(new String[]{context().getString(R.string.exclude_from_group)},
                (dialog, which) -> ContextApp.getApp(context()).getGroupsStorage()
                        .excludeMember(item(), actionListener.getGroup(), result -> {
                            if (result) {
                                actionListener.memberExcluded(item());
                                Toast.makeText(context(), R.string.member_excluded, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context(), R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                        }));

        builder.create().show();
    }

    @Layout(R.layout.members_list_item)
    static class Extractor extends UniversalAdapter.VHolderExtractor<User>
    {
        private MembersListAction actionListener;

        Extractor(MembersListAction actionListener) {
            this.actionListener = actionListener;
        }

        @Override
        public UniversalViewHolder<User> extract(View view) {
            return new MembersViewHolder(actionListener, view);
        }
    }
}