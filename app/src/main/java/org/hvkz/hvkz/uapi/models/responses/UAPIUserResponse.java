package org.hvkz.hvkz.uapi.models.responses;

import org.hvkz.hvkz.uapi.models.entities.UserProfile;

public class UAPIUserResponse
{
    public UserProfile getUser() {
        return users[0];
    }

    private UserProfile[] users;

    public boolean hasProfile() {
        return users != null;
    }
}
