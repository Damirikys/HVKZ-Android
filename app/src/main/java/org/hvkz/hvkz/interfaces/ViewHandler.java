package org.hvkz.hvkz.interfaces;

import android.content.Context;

import org.hvkz.hvkz.models.ViewBinder;

public abstract class ViewHandler implements Destroyable
{
    private BaseWindow activity;

    public ViewHandler(BaseWindow baseWindow) {
        this.activity = baseWindow;
        ViewBinder.handle(this, activity.getActivity());
        handle(activity.getContext());
    }

    public Context context() {
        return activity.getContext();
    }

    protected BaseWindow getActivity() {
        return activity;
    }

    protected abstract void handle(Context context);

    @Override
    public void onDestroy() {
        activity = null;
    }
}
