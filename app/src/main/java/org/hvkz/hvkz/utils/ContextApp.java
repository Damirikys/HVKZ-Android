package org.hvkz.hvkz.utils;

import android.content.Context;

import org.hvkz.hvkz.HVKZApp;

public abstract class ContextApp
{
    public static HVKZApp getApp(Context context) {
        return (HVKZApp) context.getApplicationContext();
    }
}
