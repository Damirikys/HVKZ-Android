package org.hvkz.hvkz.modules.chats;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.groups.GroupsDb;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.FragmentContainer;

public class ChatsViewHandler extends ViewHandler
{
    @BindView(R.id.groupsRecyclerView)
    private RecyclerView groupsRecyclerView;
    private View navigationView;

    private ChatRouter router;
    private GroupsAdapter groupsAdapter;

    public ChatsViewHandler(BaseWindow activity) {
        super(activity);
        FragmentContainer fragmentContainer = (FragmentContainer) ((Fragment) activity).getParentFragment();
        this.navigationView = fragmentContainer.getActivity().findViewById(R.id.navigation);
        this.router = (ChatRouter) fragmentContainer.getPresenter();
    }

    @Override
    protected void handle(Context context) {
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        groupsRecyclerView.setNestedScrollingEnabled(false);

        GroupsDb.getMyGroups(value ->
                groupsRecyclerView.setAdapter(
                        groupsAdapter = new GroupsAdapter(value, group -> {
                            navigationView.setVisibility(View.GONE);
                            router.moveToChat(ChatType.MULTI_USER_CHAT, group.getGroupName());
                        })
                )
        );
    }
}
