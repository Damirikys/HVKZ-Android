package org.hvkz.hvkz.uapi.models.entities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class UnknownUser implements User
{
    @Override
    public int getUserId() {
        return 0;
    }

    @Override
    public int getGroupId() {
        return 0;
    }

    @Override
    public String getGroupName() {
        return "неизвестно";
    }

    @Override
    public TokenManager getDevices() {
        return new TokenManager();
    }

    @Override
    public UserData getUserData() {
        return new UserData("", "", "");
    }

    @Override
    public String getShortName() {
        return "Неизвестный";
    }

    @Override
    public long getRegTimestamp() {
        return System.currentTimeMillis();
    }

    @NonNull
    @Override
    public String getEmail() {
        return "unknown@who.is";
    }

    @NonNull
    @Override
    public String getPhoneNumber() {
        return "89000000000";
    }

    @Override
    public boolean isEmailVerified() {
        return false;
    }

    @Override
    public String getUid() {
        return "";
    }

    @Override
    public String getProviderId() {
        return "";
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return "Неизвестная личность";
    }

    @NonNull
    @Override
    public Uri getPhotoUrl() {
        return Uri.parse("http://hvkz.org/images/noavatar.jpg");
    }
}
