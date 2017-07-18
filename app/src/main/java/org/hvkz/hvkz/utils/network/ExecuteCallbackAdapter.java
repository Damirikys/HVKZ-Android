package org.hvkz.hvkz.utils.network;

public abstract class ExecuteCallbackAdapter<T> implements FBStorageExecutor.ExecuteCallback<T>
{
    @Override
    public void onUploaded(T uploaded) {
    }

    @Override
    public void onRemoved() {
    }

    @Override
    public void onFailure(Exception e) {
    }

    @Override
    public void onProgress(long value, long max) {
    }
}
