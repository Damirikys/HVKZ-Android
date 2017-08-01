package org.hvkz.hvkz.uapi.entities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uapi.extensions.Parameters;
import org.hvkz.hvkz.uapi.extensions.TokensAgent;
import org.hvkz.hvkz.uapi.extensions.UserData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressWarnings({"unused", "NullableProblems"})
public class UserEntity implements User
{
    private transient String shortName;
    private transient TokensAgent tokensAgent;

    private String birthday;
    private int age;
    private String city;
    private String country;
    private String email;
    private String gender;
    private UserGroupEntity group;
    private String name;
    private String phone;
    private String photo;
    private String regdate;
    private String signature;
    private int uid;
    private String user;
    private String programStartDate;
    private boolean promo;
    private Parameters parameters;

    @Override
    public int getUserId() {
        return uid;
    }

    @Override
    public int getGroupId() {
        return group.id;
    }

    @Override
    public String getGroupName() {
        return group.name;
    }

    @Override
    public TokensAgent getDevices() {
        return tokensAgent;
    }

    @Override
    public UserData getUserData() {
        return new UserData(signature, parameters.getChest(), parameters.getUnderchest());
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public String getShortName() {
        if (shortName == null && getDisplayName() != null) {
            String[] parts = getDisplayName().split("\\s+");
            if (parts.length == 3) {
                shortName = parts[1] + " " + parts[2];
            } else {
                shortName = getDisplayName();
            }
        }

        return shortName;
    }

    @Override
    public long getRegTimestamp() {
        return Long.valueOf(regdate);
    }

    @Override
    public long getActivatedTimestamp() {
        if (programStartDate.isEmpty())
            return -1;

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return format.parse(programStartDate).getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    @NonNull
    @Override
    public String getEmail() {
        return email;
    }

    @NonNull
    @Override
    public String getPhoneNumber() {
        return phone;
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    }

    @Override
    public String getUid() {
        return String.valueOf(uid);
    }

    @Override
    public String getProviderId() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return name;
    }

    @NonNull
    @Override
    public Uri getPhotoUrl() {
        return Uri.parse(photo);
    }

    public void setDevices(TokensAgent manager) {
        this.tokensAgent = manager;
    }

    public String getCity() {
        return city;
    }

    private static class UserGroupEntity
    {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
