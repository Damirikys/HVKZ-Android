package org.hvkz.hvkz.modules;

import android.content.Intent;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.models.FragmentContainer;
import org.hvkz.hvkz.modules.chats.ChatRouter;
import org.hvkz.hvkz.modules.profile.ProfileFragment;

public class NavigationController implements BottomNavigationView.OnNavigationItemSelectedListener
{
    private @IdRes int containerId;
    private FragmentManager fragmentManager;

    public NavigationController(@IdRes int containerId, FragmentManager fragmentManager) {
        this.containerId = containerId;
        this.fragmentManager = fragmentManager;
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

        //transaction = transaction.setCustomAnimations(enter, exit);

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

    public void onResultReceived(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : fragmentManager.getFragments()) {
            try {
                ((AppFragment<IBasePresenter>) fragment).getPresenter()
                        .onResultReceive(requestCode, resultCode, data);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    public interface FragmentExtractor {
        Fragment extract();
    }
}
