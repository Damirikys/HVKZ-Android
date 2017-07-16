package org.hvkz.hvkz.models;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.Destroyable;

public abstract class Router implements Destroyable
{
    private FragmentManager fragmentManager;
    protected final int containerId = R.id.fragmentContainer;

    public Router handleFragmentManager(FragmentManager manager) {
        fragmentManager = manager;
        getBaseTransaction(manager.beginTransaction()).commit();
        return this;
    }

    protected abstract FragmentTransaction getBaseTransaction(FragmentTransaction transaction);

    protected FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    @Override
    public void onDestroy() {
        fragmentManager = null;
    }
}
