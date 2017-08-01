package org.hvkz.hvkz.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.modules.chats.ChatRouter;
import org.hvkz.hvkz.modules.menu.MenuFragment;
import org.hvkz.hvkz.modules.profile.ProfileFragment;
import org.hvkz.hvkz.router.RouteChannel;
import org.hvkz.hvkz.templates.BasePresenter;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.uimodels.AppFragment;
import org.hvkz.hvkz.uimodels.FragmentContainer;
import org.hvkz.hvkz.utils.ContextApp;
import org.hvkz.hvkz.xmpp.ConnectionService;

class NavigationPresenter extends BasePresenter<NavigationPresenter> implements
        BottomNavigationView.OnNavigationItemSelectedListener
{
    private @IdRes int containerId;
    private FragmentManager fragmentManager;

    private ProfileFragment profileFragment;
    private FragmentContainer chatsContainer;
    private MenuFragment menuFragment;

    private boolean isAccepted;

    NavigationPresenter(BaseWindow<NavigationPresenter> baseWindow, @IdRes int containerId, FragmentManager fragmentManager) {
        super(baseWindow);
        this.containerId = containerId;
        this.fragmentManager = fragmentManager;
        RouteChannel.clear();
    }

    @Override
    public void init() {
        HVKZApp app = ContextApp.getApp(context());
        app.bindConnectionService(ConnectionService::tryConnect);
        NavigationViewHandler viewHandler = getViewHandler();
        Bundle routeBundle = activity().getIntent().getBundleExtra("route");
        boolean isRoute = routeBundle != null;

        if (isRoute) {
            app.getUAPIclient().getUserById(app.getCurrentUser().getUserId(),
                    user -> installFragments(app, viewHandler, true));
        } else {
            installFragments(app, viewHandler, false);
        }
    }

    private void installFragments(HVKZApp app, NavigationViewHandler viewHandler, boolean isRoute) {
        profileFragment = AppFragment.instanceOf(ProfileFragment.class);
        chatsContainer = FragmentContainer.with(new ChatRouter());

        app.getOptionsStorage().isAccepted(accepted -> postUI(() -> {
            isAccepted = accepted;

            if (isAccepted) {
                menuFragment = AppFragment.instanceOf(MenuFragment.class);

                viewHandler.setSelectedItem(R.id.navigation_notifications);
                if (!isRoute) viewHandler.setSelectedItem(R.id.navigation_dashboard);
                viewHandler.setSelectedItem(R.id.navigation_home);
            } else {
                if (!isRoute) viewHandler.setSelectedItem(R.id.navigation_dashboard);
                viewHandler.setSelectedItem(R.id.navigation_home);

                getViewHandler().baseWindow().dialogMessage(
                        string(R.string.hellow_alert), string(R.string.if_not_member_notification)
                );
            }

            if (isRoute) {
                viewHandler.setSelectedItem(R.id.navigation_dashboard);
            }
        }));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                showFragment(profileFragment);
                return true;
            case R.id.navigation_dashboard:
                showFragment(chatsContainer);
                return true;
            case R.id.navigation_notifications:
                if (isAccepted) {
                    showFragment(menuFragment);
                } else {
                    Toast.makeText(context(), R.string.only_for_members, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return false;
    }

    private void showFragment(Fragment tFragment) {
        if (fragmentManager.getFragments() != null) {
            for (Fragment fragm : fragmentManager.getFragments()) {
                fragmentManager.beginTransaction()
                        .hide(fragm)
                        .commit();
            }
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(tFragment.getClass().getName());
        if (fragment != null) {
            transaction = transaction.show(fragment);
        } else {
            transaction = transaction.add(containerId, tFragment, tFragment.getClass().getName());
        }

        transaction.commit();
    }

    void onBackPressed(Callback<Void> callback) {
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

    private class NavigationViewHandler extends ViewHandler<NavigationPresenter>
    {
        @BindView(R.id.content)
        private View contentView;

        @BindView(R.id.navigation)
        private BottomNavigationView navigation;

        NavigationViewHandler(BaseWindow<NavigationPresenter> baseWindow) {
            super(baseWindow);
        }

        @Override
        protected void handle(Context context) {
            navigation.setOnNavigationItemSelectedListener(NavigationPresenter.this);
        }

        void setSelectedItem(@IdRes int id) {
            navigation.setSelectedItemId(id);

            postDelayedUI(() -> {
                if (contentView.getVisibility() == View.INVISIBLE)
                    contentView.setVisibility(View.VISIBLE);
            }, 500);
        }
    }
}
