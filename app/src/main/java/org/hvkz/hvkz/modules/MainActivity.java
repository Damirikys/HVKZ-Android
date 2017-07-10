package org.hvkz.hvkz.modules;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.widget.FrameLayout;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.models.AppActivity;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.modules.profile.ProfileFragment;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.LocalBinder;

@SuppressWarnings("unchecked")
@Layout(R.layout.activity_main)
public class MainActivity extends AppActivity<IBasePresenter>
{
    public static final int GALLERY_REQUEST = 1;

    private static final String TAG = "MainActivity";

    @BindView(R.id.fragmentContainer)
    private FrameLayout fragmentContainer;

    @BindView(R.id.navigation)
    private BottomNavigationView navigation;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_dashboard:
                return true;
            case R.id.navigation_notifications:
                return true;
        }
        return false;
    };

    private Intent serviceIntent;
    private LocalBinder<ConnectionService> serviceBinder;

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            serviceBinder = (LocalBinder<ConnectionService>) binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HVKZApp.component().inject(this);
        startService(serviceIntent =  new Intent(this, ConnectionService.class));

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, AppFragment.newInstance(ProfileFragment.class))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(serviceIntent, mServerConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        ((AppFragment<IBasePresenter>) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer))
                .getPresenter()
                .onResultReceive(requestCode, resultCode, imageReturnedIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServerConn);
    }

    @Override
    protected IBasePresenter createPresenter() {
        return new IBasePresenter()
        {
            @Override
            public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
            }

            @Override
            public void onDestroy() {
            }
        };
    }
}
