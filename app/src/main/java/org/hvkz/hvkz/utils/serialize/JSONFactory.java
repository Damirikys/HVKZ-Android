package org.hvkz.hvkz.utils.serialize;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class JSONFactory
{
    private static final Gson GSON = new Gson();

    public static <T> String toJson(T object) {
        return GSON.toJson(object, new TypeToken<T>(){}.getType());
    }

    public static <T> T fromJson(String data, Class<T> tClass) {
        return GSON.fromJson(data, tClass);
    }

    public static <T> T fromJson(String data, Type type) {
        return GSON.fromJson(data, type);
    }
}
