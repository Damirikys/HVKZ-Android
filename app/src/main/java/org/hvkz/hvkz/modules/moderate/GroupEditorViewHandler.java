package org.hvkz.hvkz.modules.moderate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.templates.UniversalAdapter;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.ContextApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GroupEditorViewHandler extends ViewHandler<GroupEditorPresenter>
{
    private String defaultTitle;

    @BindView(R.id.title)
    private TextView titleView;

    @BindView(R.id.usersRecyclerView)
    private RecyclerView recyclerView;

    @BindView(R.id.nameEditText)
    private EditText userSearchInput;

    @BindView(R.id.noticeInput)
    private EditText noticeInput;

    private Set<Integer> selectedUsers;
    private UniversalAdapter<User> adapter;
    private Group group;

    GroupEditorViewHandler(BaseWindow<GroupEditorPresenter> baseWindow) {
        super(baseWindow);
    }

    @Override
    protected void handle(Context context) {
        Fragment fragment = window();
        Bundle args = fragment.getArguments();
        this.selectedUsers = new HashSet<>();

        if (args != null) {
            defaultTitle = string(R.string.editing);
            String groupName = args.getString(GroupEditorFragment.KEY);
            ContextApp.getApp(context).getGroupsStorage().getGroupByName(groupName, group -> {
                this.group = group;
                this.noticeInput.setText(group.getNotice());

                for (int i = 0; i < group.getMembers().size(); i++) {
                    selectedUsers.add(group.getMembers().keyAt(i));
                }

                this.init(selectedUsers);
            });
        } else {
            defaultTitle = string(R.string.creating_a_group);
            init(selectedUsers);
        }
    }

    private void init(Set<Integer> users) {
        UsersViewHolder.Extractor extractor = new UsersViewHolder.Extractor(selectedUsers -> {
            if (selectedUsers.size() == 0) {
                titleView.setText(defaultTitle);
            } else {
                titleView.setText(string(R.string.selected) + " :" + selectedUsers.size());
            }
        }, users);

        titleView.setText(defaultTitle);
        recyclerView.setLayoutManager(new LinearLayoutManager(context()));
        userSearchInput.addTextChangedListener(new TextWatcherAdapter()
        {
            private List<User> searchResult = new ArrayList<>();

            @Override
            public void afterTextChanged(Editable s) {
                searchResult.clear();

                if (s.toString().isEmpty()) {
                    adapter.backupData();
                    return;
                }

                for (User user : adapter.getDefaultData()) {
                    String query = s.toString().toLowerCase();
                    if (user.getDisplayName().toLowerCase().contains(query)
                            || user.getEmail().toLowerCase().contains(query)
                            || String.valueOf(user.getUserId()).contains(query)
                            || String.valueOf(user.getPhoneNumber()).contains(query)) {
                        searchResult.add(user);
                    }
                }

                adapter.updateData(searchResult);
            }
        });


        ContextApp.getApp(context())
                .getUsersStorage()
                .getUsersDatabase(value -> postUI(() -> recyclerView.setAdapter(
                        adapter = new UniversalAdapter<>(value, extractor)
                )));
    }

    @OnClick(R.id.completeButton)
    public void onCompleteClick(View view) {
        String notice = noticeInput.getText().toString();
        if (!notice.isEmpty()) {
            if (group != null) {
                group.setNotice(noticeInput.getText().toString());
                presenter().editGroup(group, selectedUsers);
            } else {
                presenter().createGroup(notice, selectedUsers);
            }
        } else {
            Toast.makeText(context(), R.string.enter_description_group, Toast.LENGTH_SHORT).show();
        }
    }

    void closeWindow() {
        GroupEditorFragment fragment = window();
        fragment.closeWindow();
    }
}
