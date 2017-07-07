package org.hvkz.hvkz.interfaces;

public interface Thenable<T>
{
    void onSuccess(T response);
    void onError(Throwable t);
}
