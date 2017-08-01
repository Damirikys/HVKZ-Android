package org.hvkz.hvkz.modules.chats.window.members;

import android.app.Dialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.templates.UniversalAdapter;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.Tools;

import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class MembersListWrapper implements MembersListAction
{
    private Group group;
    private Dialog dialog;
    private RecyclerView recyclerView;

    private MembersListWrapper(Dialog dialog, Group group) {
        this.dialog = dialog;
        this.group = group;
        this.recyclerView = new RecyclerView(dialog.getContext());
        this.recyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
        this.recyclerView.setAdapter(new UniversalAdapter<>(
                Tools.asList(group.getMembers()),
                new MembersViewHolder.Extractor(this))
        );
        this.recyclerView.setPadding(0, dpToPx(dialog.getContext().getResources().getDisplayMetrics(), 16), 0, 0);
    }

    private View getView() {
        return recyclerView;
    }

    public static View inflate(Dialog dialog, Group group) {
        return new MembersListWrapper(dialog, group)
                .getView();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }

    @Override
    public void memberExcluded(User user) {
        SparseArray<User> members = group.getMembers();
        members.remove(user.getUserId());
        group.setMembers(members);
        dismiss();

        EventChannel.send(group);
    }

    @Override
    public Group getGroup() {
        return group;
    }
}
