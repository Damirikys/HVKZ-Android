package org.hvkz.hvkz.event;

import android.os.Handler;
import android.os.Looper;

import org.hvkz.hvkz.annotations.EventReceiver;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public abstract class EventChannel
{
    private EventChannel(){}

    private static final Set<Object> receivers = new HashSet<>();
    private static final Handler handler = new Handler(Looper.myLooper());

    public static void connect(Object receiver) {
        receivers.add(receiver);
    }

    public static void disconnect(Object receiver) {
        receivers.remove(receiver);
    }

    public static void send(Object data) {
        for (Object receiver : receivers) {
            new Thread(() -> {
                for (Method method : receiver.getClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(EventReceiver.class)) {
                        handler.post(() -> {
                            try { method.invoke(receiver, data);}
                            catch (Exception ignored) {}
                        });
                    }
                }
            }).start();
        }
    }
}
