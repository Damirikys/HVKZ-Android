package org.hvkz.hvkz.uapi.models.entities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hvkz.hvkz.utils.serialize.JSONFactory;

public class UserProfile implements User
{
    private int uid;
    private Group group;

    private String user;
    private String email;
    private String full_name;
    private String signature;
    private String reg_date_timestamp;
    private String avatar;
    private String city;
    private String birthday;
    private Gender gender;
    private String country;
    private String email_verified;
    private long phone;

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
        return full_name;
    }

    @Nullable
    @Override
    public Uri getPhotoUrl() {
        return Uri.parse(avatar);
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

    public String getSignature() {
        return signature;
    }

    public String getRegDate() {
        return reg_date_timestamp;
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
