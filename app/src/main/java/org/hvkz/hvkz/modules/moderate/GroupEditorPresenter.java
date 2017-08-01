package org.hvkz.hvkz.modules.moderate;

import android.support.annotation.NonNull;
import android.widget.Toast;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.db.GroupsStorage;
import org.hvkz.hvkz.firebase.db.UsersStorage;
import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.utils.Tools;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.config.XMPPConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupEditorPresenter extends BasePresenter<GroupEditorPresenter>
{
    private final int STATUS_CREATED = 201;
    private final MediaType JSON_TYPE;
    private final OkHttpClient client;

    @Inject
    User user;

    @Inject
    UsersStorage usersStorage;

    @Inject
    GroupsStorage groupStorage;

    GroupEditorPresenter(BaseWindow<GroupEditorPresenter> activity) {
        super(activity);
        ContextApp.getApp(activity.getContext()).component().inject(this);
        JSON_TYPE = MediaType.parse(string(R.string.mediatype_json));
        client = new OkHttpClient();
    }

    void createGroup(String notice, Set<Integer> members) {
        getViewHandler().baseWindow().showProgress(string(R.string.please_wait));

        int NAME_LENGTH = 10;
        String roomName = Tools.nonce(NAME_LENGTH);
        String requestUrl = XMPPConfiguration.SERVER + string(R.string.rest_chatrooms);
        String authorization = string(R.string.authorization);
        String authToken = string(R.string.authtoken);

        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader(authorization, authToken)
                .post(RequestBody.create(JSON_TYPE, JSONFactory.toJson(RoomRequest.create(roomName, notice))))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() == STATUS_CREATED) {
                    GroupsStorage.GroupItem group = new GroupsStorage.GroupItem();
                    group.admin = user.getUserId();
                    group.members = new ArrayList<>(members);
                    group.notice = notice;

                    groupStorage.createGroup(roomName, group, result -> {
                        if (result) {
                            onGroupCreateSuccess();
                        } else {
                            onGroupCreateFailed();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                onGroupCreateFailed();
            }
        });
    }

    void editGroup(Group editedGroup, Set<Integer> users) {
        getViewHandler().baseWindow().showProgress(string(R.string.please_wait));

        GroupsStorage.GroupItem group = new GroupsStorage.GroupItem();
        group.admin = editedGroup.getAdmin().getUserId();
        group.members = new ArrayList<>(users);
        group.notice = editedGroup.getNotice();

        groupStorage.createGroup(editedGroup.getGroupName(), group, result -> {
            if (result) {
                onGroupCreateSuccess();
            } else {
                onGroupCreateFailed();
            }
        });
    }

    private void onGroupCreateSuccess() {
        GroupEditorViewHandler viewHandler = getViewHandler();
        viewHandler.closeWindow();
        viewHandler.baseWindow().hideProgress();
        Toast.makeText(context(), R.string.complete, Toast.LENGTH_SHORT).show();
    }

    private void onGroupCreateFailed() {
        getViewHandler().baseWindow().hideProgress();
        Toast.makeText(context(), R.string.failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected ViewHandler<GroupEditorPresenter> createViewHandler(BaseWindow<GroupEditorPresenter> activity) {
        return new GroupEditorViewHandler(activity);
    }

    @SuppressWarnings("unused")
    private static class RoomRequest {
        private String roomName;
        private String naturalName;
        private String description;
        private final String persistent = "true";
        private final String publicRoom = "true";
        private final String membersOnly = "false";
        private final String logEnabled = "true";
        private final String registrationEnabled = "true";
        private final String canAnyoneDiscoverJID = "true";
        private final String loginRestrictedToNickname = "true";

        public static RoomRequest create(String roomName, String notice) {
            RoomRequest roomRequest = new RoomRequest();
            roomRequest.roomName = roomName;
            roomRequest.naturalName = roomName;
            roomRequest.description = notice;

            return roomRequest;
        }
    }
}
