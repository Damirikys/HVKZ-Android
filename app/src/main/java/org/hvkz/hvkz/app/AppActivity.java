package org.hvkz.hvkz.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.hvkz.hvkz.app.annotations.Layout;
import org.hvkz.hvkz.app.annotations.OnClick;
import org.hvkz.hvkz.app.annotations.OnLongClick;
import org.hvkz.hvkz.app.annotations.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Layout
public abstract class AppActivity<T> extends AppCompatActivity
{
    private T presenter;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<? extends AppActivity> clazz = getClass();
        setContentView(clazz.getAnnotation(Layout.class).value());

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(View.class)) {
                field.setAccessible(true);
                try {
                    field.set(this, findViewById(field.getAnnotation(View.class).value()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OnClick.class)) {
                findViewById(method.getAnnotation(OnClick.class).value()).setOnClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(this));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (method.isAnnotationPresent(OnLongClick.class)) {
                findViewById(method.getAnnotation(OnLongClick.class).value()).setOnLongClickListener(v -> {
                    try {
                        method.invoke(clazz.cast(this));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    return true;
                });
            }
        }

        presenter = createPresenter();
    }

    protected abstract T createPresenter();

    protected T getPresenter() {
        if (presenter == null) {
            presenter = createPresenter();
        }

        return presenter;
    }
}
