package org.hvkz.hvkz.sync;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppActivity;

@Layout(R.layout.activity_settings)
public class SyncActivity extends AppActivity<SyncPresenter>
{
    @Override
    protected SyncPresenter createPresenter() {
        return new SyncPresenter(this);
    }
}
