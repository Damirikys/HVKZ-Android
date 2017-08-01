package org.hvkz.hvkz.modules.chats;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.GroupsStorage;
import org.hvkz.hvkz.firebase.db.OptionsStorage;
import org.hvkz.hvkz.firebase.db.UsersStorage;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.modules.chats.chatsroster.ContactsAdapter;
import org.hvkz.hvkz.modules.chats.chatsroster.GroupsPagerAdapter;
import org.hvkz.hvkz.modules.moderate.GroupEditorFragment;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.utils.ContextApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import static org.hvkz.hvkz.firebase.db.GroupsStorage.LOCAL;

@SuppressWarnings("WeakerAccess")
public class ChatsViewHandler extends ViewHandler<ChatsPresenter>
{
    @BindView(R.id.groupsPager)
    private ViewPager groupsViewPager;

    @BindView(R.id.view_pager_indicator)
    private ViewPagerIndicator viewPagerIndicator;

    @BindView(R.id.contactsRecyclerView)
    private RecyclerView contactsRecyclerView;

    @BindView(R.id.notgroup_info)
    private View notgroupView;

    @BindView(R.id.fabAdminPanel)
    private FloatingActionButton createGroupButton;

    @Inject
    GroupsStorage groupsStorage;

    @Inject
    OptionsStorage optionsStorage;

    @Inject
    UsersStorage usersStorage;

    @Inject
    MessagesStorage messagesStorage;

    private GroupsPagerAdapter groupsAdapter;
    private ContactsAdapter contactsAdapter;

    ChatsViewHandler(BaseWindow<ChatsPresenter> activity) {
        super(activity);
    }

    @Override
    protected void handle(Context context) {
        ContextApp.getApp(context).component().inject(this);

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        contactsRecyclerView.setNestedScrollingEnabled(false);

        groupsStorage.getMyGroups(LOCAL, groups -> {
            if (groups.isEmpty()) {
                notgroupView.setVisibility(View.VISIBLE);
            } else {
                groupsAdapter = new GroupsPagerAdapter(groupsViewPager, viewPagerIndicator, groups);
            }
        });

        optionsStorage.getOptions(options -> postUI(() -> {
            Set<Integer> contacts = new HashSet<>();
            contacts.add(options.support);

            for (String chat : messagesStorage.getExistedChats()) {
                try {
                    contacts.add(Integer.valueOf(chat));
                } catch (NumberFormatException ignored) {}
            }

            usersStorage.getProfilesFromCache(new ArrayList<>(contacts), value -> {
                if (contactsAdapter != null) contactsAdapter.onDestroy();
                contactsRecyclerView.setAdapter(contactsAdapter = new ContactsAdapter(context, value));
            });
        }));

       optionsStorage.isModerated(value -> {
            if (value) {
                createGroupButton.setBackgroundResource(R.drawable.ic_plus);
                createGroupButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick(R.id.fabAdminPanel)
    public void onAdminPanelOpen(View view) {
        GroupEditorFragment dialogFrag = GroupEditorFragment.newInstance(null);
        dialogFrag.setParentFab(createGroupButton);
        dialogFrag.show(window(Fragment.class).getFragmentManager(), dialogFrag.getTag());
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
