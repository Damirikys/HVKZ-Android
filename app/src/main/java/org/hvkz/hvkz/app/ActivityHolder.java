package org.hvkz.hvkz.app;

import android.app.Activity;

import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;
import org.hvkz.hvkz.annotations.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ActivityHolder
{
    private ActivityHolder(){}

    public static <T> void hold(T target, Activity activity) {
        Class clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(View.class)) {
                field.setAccessible(true);
                try {
                    field.set(target, activity.findViewById(field.getAnnotation(View.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnClick.class)) {
                activity.findViewById(method.getAnnotation(OnClick.class).value()).setOnClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(activity));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                activity.findViewById(method.getAnnotation(OnLongClick.class).value()).setOnLongClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(activity));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                });
            }
        }
    }
}
