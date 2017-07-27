package org.hvkz.hvkz.modules.chats.window;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.models.ViewBinder;
import org.hvkz.hvkz.modules.RouteChannel;
import org.hvkz.hvkz.modules.chats.ChatType;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.JidFactory;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.hvkz.hvkz.modules.chats.ChatRouter.CHAT_TYPE_KEY;
import static org.hvkz.hvkz.modules.chats.ChatRouter.DOMAIN_KEY;

public class MembersListAdapter extends RecyclerView.Adapter<MembersListAdapter.MemberViewHolder>
{
    private Destroyable container;
    private List<User> members;

    public MembersListAdapter(Destroyable container, List<User> members) {
        this.members = members;
        this.container = container;
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        holder.hold(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder
    {
        private User user;

        @BindView(R.id.dialogAvatar)
        private CircleImageView photoView;

        @BindView(R.id.dialogName)
        private TextView nameView;

        @BindView(R.id.lastMessage)
        private TextView emailView;

        @BindView(R.id.status_online)
        private View statusView;

        public MemberViewHolder(View itemView) {
            super(itemView);
            ViewBinder.handle(this, itemView);
        }

        private void hold(User user) {
            this.user = user;
            this.nameView.setText(user.getDisplayName());
            this.emailView.setText(user.getEmail());

            Glide.with(context())
                    .load(user.getPhotoUrl())
                    .centerCrop()
                    .into(photoView);

            ContextApp.getApp(context()).bindConnectionService(service -> {
                if(service.getRoster().getPresence(JidFactory.from(user)).isAvailable()) {
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
            bundle.putString(DOMAIN_KEY, String.valueOf(user.getUserId()));
            RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(bundle));

            container.onDestroy();
        }

        private Context context() {
            return photoView.getContext();
        }
    }
}
