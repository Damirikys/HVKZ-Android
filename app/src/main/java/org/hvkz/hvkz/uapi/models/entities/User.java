package org.hvkz.hvkz.uapi.models.entities;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.auth.UserInfo;

public interface User extends UserInfo
{
    int getUserId();

    int getGroupId();

    String getGroupName();

    TokenManager getDevices();

    UserData getUserData();

    String getShortName();

    @NonNull
    @Override
    String getEmail();

    @NonNull
    @Override
    String getPhoneNumber();

    @NonNull
    @Override
    Uri getPhotoUrl();
}
