package org.hvkz.hvkz.uapi.responses;

import org.hvkz.hvkz.uapi.entities.UAPIUser;

@SuppressWarnings("unused")
public class UAPIUserResponse
{
    public UAPIUser getUser() {
        return users[0];
    }

    private UAPIUser[] users;

    public boolean hasProfile() {
        return users != null;
    }
}
