package org.hvkz.hvkz.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.ViewHandler;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.models.BasePresenter;
import org.hvkz.hvkz.models.FragmentContainer;
import org.hvkz.hvkz.modules.chats.ChatRouter;
import org.hvkz.hvkz.modules.menu.MenuFragment;
import org.hvkz.hvkz.modules.profile.ProfileFragment;

public class NavigationPresenter extends BasePresenter<NavigationPresenter> implements
        BottomNavigationView.OnNavigationItemSelectedListener
{
    private @IdRes int containerId;
    private FragmentManager fragmentManager;

    public NavigationPresenter(BaseWindow<NavigationPresenter> baseWindow,
                               @IdRes int containerId,
                               FragmentManager fragmentManager) {
        super(baseWindow);
        this.containerId = containerId;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void init() {
        RouteChannel.clear();

        NavigationPresenter.this.selectFragmentClass(
                ProfileFragment.class,
                () -> AppFragment.instanceOf(ProfileFragment.class),
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        Bundle routeBundle = activity()
                .getIntent()
                .getBundleExtra("route");

        if (routeBundle != null) {
            getViewHandler(NavigationViewHandler.class)
                    .setSelectedItem(R.id.navigation_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                selectFragmentClass(
                        ProfileFragment.class,
                        () -> AppFragment.instanceOf(ProfileFragment.class),
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                return true;
            case R.id.navigation_dashboard:
                selectFragmentClass(
                        FragmentContainer.class,
                        () -> FragmentContainer.with(new ChatRouter()),
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                return true;
            case R.id.navigation_notifications:
                selectFragmentClass(
                        MenuFragment.class,
                        () -> AppFragment.instanceOf(MenuFragment.class),
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                return true;
        }
        return false;
    }

    public <T extends Fragment> void selectFragmentClass(Class<T> tClass,
                                                          FragmentExtractor extractor,
                                                          @AnimRes int enter,
                                                          @AnimRes int exit) {
        Fragment fragment = fragmentManager.findFragmentByTag(tClass.getName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (fragmentManager.getFragments() != null) {
            for (Fragment fragm : fragmentManager.getFragments()) {
                fragmentManager.beginTransaction()
                        .hide(fragm)
                        .commit();
            }
        }

        if (fragment != null) {
            transaction = transaction.show(fragment);
        } else {
            transaction = transaction.add(containerId, extractor.extract(), tClass.getName());
        }

        transaction.commit();
    }

    public void onBackPressed(Callback<Void> callback) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            FragmentManager manager = fragment.getChildFragmentManager();
            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStack();
                return;
            }
        }

        if (fragmentManager.getBackStackEntryCount() < 0) {
            fragmentManager.popBackStack();
        } else {
            callback.call(null);
        }
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        super.onResultReceive(requestCode, resultCode, dataIntent);
        for (Fragment fragment : fragmentManager.getFragments()) {
            try {
                AppFragment.of(fragment)
                        .getPresenter()
                        .onResultReceive(requestCode, resultCode, dataIntent);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected ViewHandler<NavigationPresenter> createViewHandler(BaseWindow<NavigationPresenter> activity) {
        return new NavigationViewHandler(activity);
    }

    public interface FragmentExtractor {
        Fragment extract();
    }

    private class NavigationViewHandler extends ViewHandler<NavigationPresenter> {

        @BindView(R.id.navigation)
        private BottomNavigationView navigation;

        NavigationViewHandler(BaseWindow<NavigationPresenter> baseWindow) {
            super(baseWindow);
        }

        @Override
        protected void handle(Context context) {
            navigation.setOnNavigationItemSelectedListener(NavigationPresenter.this);
        }

        public void setSelectedItem(@IdRes int id) {
            navigation.setSelectedItemId(id);
        }
    }
}
