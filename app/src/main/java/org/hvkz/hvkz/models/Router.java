package org.hvkz.hvkz.models;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.modules.RouteChannel;

public abstract class Router implements Destroyable
{
    private FragmentManager fragmentManager;
    protected final int containerId = R.id.fragmentContainer;

    public Router() {
        RouteChannel.subscribe(this);
    }

    public Router handleFragmentManager(FragmentManager manager) {
        fragmentManager = manager;
        getBaseTransaction(manager.beginTransaction()).commit();
        return this;
    }

    public abstract void onRouteRequest(RouteChannel.RouteRequest request);

    protected abstract FragmentTransaction getBaseTransaction(FragmentTransaction transaction);

    protected FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    @Override
    public void onDestroy() {
        fragmentManager = null;
        RouteChannel.unsubscribe(this);
    }
}
