package org.hvkz.hvkz.modules;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.IBasePresenter;
import org.hvkz.hvkz.uimodels.AppActivity;

@Layout(R.layout.activity_main)
public class NavigationActivity extends AppActivity<NavigationPresenter>
{
    public static final int GALLERY_REQUEST = 1;

    @Override
    public void onBackPressed() {
        getPresenter().onBackPressed(value -> super.onBackPressed());
    }

    @Override
    protected IBasePresenter createPresenter() {
        return new NavigationPresenter(this, R.id.fragmentContainer, getSupportFragmentManager());
    }
}
