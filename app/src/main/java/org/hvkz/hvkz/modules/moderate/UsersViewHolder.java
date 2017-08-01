package org.hvkz.hvkz.modules.moderate;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.templates.UniversalAdapter;
import org.hvkz.hvkz.templates.UniversalViewHolder;
import org.hvkz.hvkz.uapi.User;

import java.util.Set;

class UsersViewHolder extends UniversalViewHolder<User>
{
    private Set<Integer> selectedUsers;
    private OnUserSelectListener listener;

    @BindView(R.id.userName)
    private TextView name;

    @BindView(R.id.userPhoto)
    private ImageView photo;

    @BindView(R.id.userEmail)
    private TextView email;

    @BindView(R.id.userPhone)
    private TextView phone;

    @BindView(R.id.circle)
    private View circle;
    @BindView(R.id.mark)
    private View mark;

    private UsersViewHolder(Set<Integer> users, OnUserSelectListener listener, View itemView) {
        super(itemView);
        this.selectedUsers = users;
        this.listener = listener;
    }

    @Override
    protected void hold() {
        if (selectedUsers.contains(item().getUserId())) {
            circle.setVisibility(View.VISIBLE);
            mark.setVisibility(View.VISIBLE);
        } else {
            circle.setVisibility(View.INVISIBLE);
            mark.setVisibility(View.INVISIBLE);
        }

        name.setText(item().getShortName());
        email.setText(item().getEmail());
        phone.setText(item().getPhoneNumber());

        Glide.with(context())
                .load(item().getPhotoUrl())
                .centerCrop()
                .into(photo);
    }

    @OnLongClick(R.id.userItem)
    public void onUserSelect(View view) {
        if (selectedUsers.contains(item().getUserId())) {
            circle.setVisibility(View.INVISIBLE);
            mark.setVisibility(View.INVISIBLE);
            selectedUsers.remove(item().getUserId());
        } else {
            circle.setVisibility(View.VISIBLE);
            mark.setVisibility(View.VISIBLE);
            selectedUsers.add(item().getUserId());
        }

        listener.selected(selectedUsers);
    }

    @Layout(R.layout.user_add_layout)
    static class Extractor extends UniversalAdapter.VHolderExtractor<User>
    {
        private final Set<Integer> users;
        private final OnUserSelectListener listener;

        Extractor(OnUserSelectListener listener, Set<Integer> users) {
            this.users = users;
            this.listener = listener;
        }

        @Override
        public UniversalViewHolder<User> extract(View view) {
            return new UsersViewHolder(users, listener, view);
        }
    }

    interface OnUserSelectListener {
        void selected(Set<Integer> selectedUsers);
    }
}
