package org.hvkz.hvkz.uapi.entities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uapi.extensions.Parameters;
import org.hvkz.hvkz.uapi.extensions.TokensAgent;
import org.hvkz.hvkz.uapi.extensions.UserData;
import org.hvkz.hvkz.utils.Tools;

@SuppressWarnings("NullableProblems")
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
    public TokensAgent getDevices() {
        return new TokensAgent();
    }

    @Override
    public UserData getUserData() {
        return new UserData("", "", "");
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    @Override
    public String getShortName() {
        return "Неизвестный";
    }

    @Override
    public long getRegTimestamp() {
        return Tools.timestamp();
    }

    @Override
    public long getActivatedTimestamp() {
        return 1;
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
