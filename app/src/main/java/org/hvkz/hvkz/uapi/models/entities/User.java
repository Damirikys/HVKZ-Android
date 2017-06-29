package org.hvkz.hvkz.uapi.models.entities;

import android.support.annotation.NonNull;

import com.google.firebase.auth.UserInfo;

public interface User extends UserInfo
{
    int getUserId();

    int getGroupId();

    String getGroupName();

    @NonNull
    @Override
    String getEmail();

    @NonNull
    @Override
    String getPhoneNumber();
}
