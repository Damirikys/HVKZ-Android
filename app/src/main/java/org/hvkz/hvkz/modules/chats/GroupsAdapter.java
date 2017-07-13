package org.hvkz.hvkz.modules.chats;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.models.ViewBinder;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>
{
    private Callback<Group> onClickListener;
    private List<Group> groups;

    public GroupsAdapter(List<Group> groups, Callback<Group> onClickListener) {
        this.groups = groups;
        this.onClickListener = onClickListener;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.group_layout, null);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.hold(group, onClickListener);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Callback<Group> onClickListener;
        private Group group;

        @BindView(R.id.groupCardView)
        private CardView groupCardView;

        @BindView(R.id.adminPhoto)
        private ImageView adminPhotoView;

        @BindView(R.id.adminName)
        private TextView adminNameView;

        @BindView(R.id.notice)
        private TextView noticeView;

        public GroupsViewHolder(View itemView) {
            super(itemView);
            ViewBinder.handle(this, itemView);
            groupCardView.setOnClickListener(this);
        }

        public void hold(Group group, Callback<Group> listener) {
            this.group = group;
            this.onClickListener = listener;
            this.adminNameView.setText(group.getAdmin().getDisplayName());
            this.noticeView.setText(group.getNotice());

            Glide.with(adminPhotoView.getContext())
                    .load(group.getAdmin().getPhotoUrl())
                    .centerCrop()
                    .fitCenter()
                    .into(adminPhotoView);
        }

        @Override
        public void onClick(View v) {
            onClickListener.call(group);
        }
    }
}
