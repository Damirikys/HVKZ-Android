package org.hvkz.hvkz.models;

import android.app.Activity;

import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.annotations.OnLongClick;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ViewBinder
{
    private ViewBinder(){}

    public static <T> void handle(T target, Activity activity) {
        Class clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(BindView.class)) {
                field.setAccessible(true);
                try {
                    field.set(target, activity.findViewById(field.getAnnotation(BindView.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnClick.class)) {
                activity.findViewById(method.getAnnotation(OnClick.class).value()).setOnClickListener(v -> {
                    try {
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                activity.findViewById(method.getAnnotation(OnLongClick.class).value()).setOnLongClickListener(v -> {
                    try {
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                });
            }
        }
    }

    public static <T> void handle(T target, android.view.View view) {
        Class clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(BindView.class)) {
                field.setAccessible(true);
                try {
                    field.set(target, view.findViewById(field.getAnnotation(BindView.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnClick.class)) {
                view.findViewById(method.getAnnotation(OnClick.class).value()).setOnClickListener(v -> {
                    try {
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                view.findViewById(method.getAnnotation(OnLongClick.class).value()).setOnLongClickListener(v -> {
                    try {
                        method.invoke(target);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                });
            }
        }
    }
}
