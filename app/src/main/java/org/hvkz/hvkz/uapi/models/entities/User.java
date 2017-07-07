package org.hvkz.hvkz.uapi.models.entities;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.auth.UserInfo;

import org.hvkz.hvkz.db.firebase.TokenManager;

public interface User extends UserInfo
{
    int getUserId();

    int getGroupId();

    String getGroupName();

    TokenManager getDevices();

    UserData getUserData();

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
