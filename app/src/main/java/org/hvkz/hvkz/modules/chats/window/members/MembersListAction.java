package org.hvkz.hvkz.modules.chats.window.members;

import org.hvkz.hvkz.firebase.entities.Group;
import org.hvkz.hvkz.uapi.User;

interface MembersListAction {
    void dismiss();
    void memberExcluded(User user);
    Group getGroup();
}