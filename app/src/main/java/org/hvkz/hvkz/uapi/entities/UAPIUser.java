package org.hvkz.hvkz.uapi.entities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Patterns;

import com.google.gson.annotations.SerializedName;

import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.uapi.extensions.Parameters;
import org.hvkz.hvkz.uapi.extensions.TokensAgent;
import org.hvkz.hvkz.uapi.extensions.UserData;
import org.hvkz.hvkz.uapi.oauth.OAuth;
import org.hvkz.hvkz.utils.serialize.JSONFactory;

@SuppressWarnings({"unused", "NullableProblems"})
public class UAPIUser implements User
{
    private transient String shortName;

    private int uid;
    private Group group;

    private String user;
    private String email;

    @SerializedName("full_name")
    private String name;

    private String signature;
    private String reg_date_timestamp;

    @SerializedName("avatar")
    private String photo;

    private String city;
    private String birthday;
    private Gender gender;
    private String country;
    private String email_verified;
    private String yahoo;
    private String state;
    private long phone;

    private TokensAgent devices;

    public UAPIUser() {}

    public UAPIUser(String name, String photoUrl, TokensAgent tokensAgent) {
        this.name = name;
        this.photo = photoUrl;
        this.devices = tokensAgent;
    }

    @Override
    public String getUid() {
        return String.valueOf(uid);
    }

    @Override
    public int getUserId() {
        return uid;
    }

    @NonNull
    @Override
    public String getPhoneNumber() {
        return String.valueOf(phone);
    }

    @Override
    public boolean isEmailVerified() {
        return email_verified.equals("yes");
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

    @Nullable
    @Override
    public Uri getPhotoUrl() {
        if (Patterns.WEB_URL.matcher(photo).matches()) {
            return Uri.parse(photo);
        } else {
            return Uri.parse(OAuth.BASE_URL + photo);
        }
    }

    @NonNull
    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public int getGroupId() {
        return group.id;
    }

    @Override
    public String getGroupName() {
        return group.name;
    }

    public TokensAgent getDevices() {
        return devices;
    }

    public void setDevices(TokensAgent devices) {
        this.devices = devices;
    }

    @Override
    public UserData getUserData() {
        return new UserData(signature, yahoo, state);
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
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
        return Long.valueOf(reg_date_timestamp);
    }

    @Override
    public long getActivatedTimestamp() {
        return 1;
    }

    public String getCity() {
        return city;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getGender() {
        return gender.name;
    }

    public String getCountry() {
        return country;
    }

    private static class Group
    {
        private int id;
        private String name;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    private static class Gender
    {
        private String name;
        private int code;

        public String getName() {
            return name;
        }

        public int getCode() {
            return code;
        }
    }

    @Override
    public String toString() {
        return JSONFactory.toJson(this);
    }
}