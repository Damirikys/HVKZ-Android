package org.hvkz.hvkz.router;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.uimodels.FragmentContainer;

public abstract class Router implements IBasePresenter
{
    protected final int containerId = R.id.fragmentContainer;

    private FragmentManager fragmentManager;
    private FragmentContainer fragmentContainer;

    public Router() {
        RouteChannel.subscribe(this);
    }

    public Router handleParent(FragmentContainer container) {
        this.fragmentContainer = container;
        this.fragmentManager = fragmentContainer.getChildFragmentManager();
        getBaseTransaction(fragmentManager.beginTransaction()).commit();
        return this;
    }

    protected FragmentContainer getFragmentContainer() {
        return fragmentContainer;
    }

    @Override
    public void init() {}

    public abstract void onRouteRequest(RouteChannel.RouteRequest request);

    protected abstract FragmentTransaction getBaseTransaction(FragmentTransaction transaction);

    protected FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    @Override
    public void onDestroy() {
        fragmentManager = null;
        fragmentContainer = null;
        RouteChannel.unsubscribe(this);
    }
}
