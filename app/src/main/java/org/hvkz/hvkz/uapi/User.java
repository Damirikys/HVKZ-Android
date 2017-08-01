package org.hvkz.hvkz.uapi;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.auth.UserInfo;

import org.hvkz.hvkz.uapi.extensions.Parameters;
import org.hvkz.hvkz.uapi.extensions.TokensAgent;
import org.hvkz.hvkz.uapi.extensions.UserData;

@SuppressWarnings({"NullableProblems", "unused"})
public interface User extends UserInfo
{
    int getUserId();

    int getGroupId();

    String getGroupName();

    TokensAgent getDevices();

    UserData getUserData();

    Parameters getParameters();

    String getShortName();

    long getRegTimestamp();

    long getActivatedTimestamp();

    @NonNull
    @Override
    String getDisplayName();

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
