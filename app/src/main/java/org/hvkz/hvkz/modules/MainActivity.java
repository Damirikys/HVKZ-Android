package org.hvkz.hvkz.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.models.AppActivity;
import org.hvkz.hvkz.utils.ContextApp;

@SuppressWarnings("unchecked")
@Layout(R.layout.activity_main)
public class MainActivity extends AppActivity<NavigationPresenter>
{
    public static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextApp.getApp(this).bindConnectionService(service -> {
            if (!service.getConnection().isAuthenticated()) {
                service.getConnection().disconnect();
                service.tryConnect();
            }
        });
    }

    @Override
    public void onBackPressed() {
        getPresenter().onBackPressed(value -> super.onBackPressed());
    }

    @Override
    protected IBasePresenter createPresenter() {
        return new NavigationPresenter(this, R.id.fragmentContainer, getSupportFragmentManager());
    }
}
