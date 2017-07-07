package org.hvkz.hvkz.modules.home;

import java.io.Serializable;

public class Photo implements Serializable
{
    private String url;
    private String description;
    private long date;

    public Photo() {}

    public Photo(String url) {
        this.url = url;
    }

    public Photo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Photo setDate(long date) {
        this.date = date;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Photo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescription() { return description; }
}

