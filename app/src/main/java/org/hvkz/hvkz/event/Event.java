package org.hvkz.hvkz.event;

public class Event<T>
{
    private EventType type;
    private T data;

    public Event(EventType type) {
        this.type = type;
    }

    public Event setData(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        USER_PROFILE_OPEN
    }
}
