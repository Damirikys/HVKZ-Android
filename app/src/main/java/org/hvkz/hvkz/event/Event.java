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
        GROUPS_DATA_WAS_CHANGED,
        USER_PROFILE_OPEN,
        UPDATE_GROUP_CHAT_WINDOW
    }
}
