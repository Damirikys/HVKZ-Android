package org.hvkz.hvkz.modules.chats;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.FragmentContainer;
import org.hvkz.hvkz.utils.ContextApp;

import java.util.LinkedList;
import java.util.List;

import static org.hvkz.hvkz.firebase.db.groups.GroupsStorage.LOCAL;

public class ChatsViewHandler extends ViewHandler<ChatsPresenter>
{
    @BindView(R.id.groupsRecyclerView)
    private RecyclerView groupsRecyclerView;

    @BindView(R.id.contactsRecyclerView)
    private RecyclerView contactsRecyclerView;

    @BindView(R.id.contacts_title)
    private TextView contactsTitle;

    private View navigationView;

    private GroupsAdapter groupsAdapter;
    private ContactsAdapter contactsAdapter;

    public ChatsViewHandler(BaseWindow<ChatsPresenter> activity) {
        super(activity);
        FragmentContainer fragmentContainer = (FragmentContainer) ((Fragment) activity).getParentFragment();
        this.navigationView = fragmentContainer.getActivity().findViewById(R.id.navigation);
    }

    @Override
    protected void handle(Context context) {
        HVKZApp hvkzApp = ContextApp.getApp(context);

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        groupsRecyclerView.setNestedScrollingEnabled(false);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        contactsRecyclerView.setNestedScrollingEnabled(false);

        List<Integer> contacts = new LinkedList<>();
        for (String chat : hvkzApp.getMessagesStorage().getExistedChats()) {
            try {
                contacts.add(Integer.valueOf(chat));
            } catch (NumberFormatException ignored) {}
        }

        if (contacts.size() > 0) {
            contactsTitle.setVisibility(View.GONE);
            hvkzApp.getUsersStorage().getProfilesFromCache(contacts, value -> {
                if (contactsAdapter != null) contactsAdapter.onDestroy();
                contactsRecyclerView.setAdapter(contactsAdapter = new ContactsAdapter(context, value));
            });
        }

        hvkzApp.getGroupsStorage()
                .getMyGroups(LOCAL, value ->
                        groupsRecyclerView.setAdapter(groupsAdapter = new GroupsAdapter(context, value)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contactsAdapter != null)
            contactsAdapter.onDestroy();
        if (groupsAdapter != null)
            groupsAdapter.onDestroy();
    }
}
