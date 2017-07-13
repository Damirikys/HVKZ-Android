package org.hvkz.hvkz.models;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.interfaces.Destroyable;

public abstract class Router implements Destroyable
{
    private FragmentManager fragmentManager;

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
