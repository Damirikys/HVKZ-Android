package org.hvkz.hvkz.uapi.models.entities;

import org.hvkz.hvkz.firebase.db.users.UsersDb;

public final class UAPIUser
{
    private static UAPIUser UAPI_USER;

    private final User currentUser;

    private UAPIUser(User user) {
        this.currentUser = user;
    }

    public static void setCurrentUser(User user) {
        UAPI_USER = new UAPIUser(user);
        UsersDb.update();
    }

    public static User getUAPIUser() {
        return UAPI_USER.currentUser;
    }
}
