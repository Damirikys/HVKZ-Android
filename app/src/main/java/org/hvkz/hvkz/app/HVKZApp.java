package org.hvkz.hvkz.app;

import android.app.Application;
import android.content.Context;

import org.hvkz.hvkz.di.DaggerIComponent;
import org.hvkz.hvkz.di.DependencyProvider;
import org.hvkz.hvkz.di.IComponent;

public class HVKZApp extends Application
{
    private IComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerIComponent
                .builder()
                .dependencyProvider(new DependencyProvider())
                .build();
    }

    public static IComponent component(Context context){
        return ((HVKZApp) context.getApplicationContext()).component;
    }
}
