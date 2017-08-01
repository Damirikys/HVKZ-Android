package org.hvkz.hvkz.uapi.extensions;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class TokensAgent
{
    private String android;
    private String ios;
    private String web;

    public String getAndroid() {
        return android;
    }

    public String getIOS() {
        return ios;
    }

    public String getWeb() {
        return web;
    }

    public List<String> getAll() {
        List<String> list = new ArrayList<>();
        if (android != null) list.add(android);
        if (ios != null) list.add(ios);
        if (web != null) list.add(web);

        return list;
    }
}
