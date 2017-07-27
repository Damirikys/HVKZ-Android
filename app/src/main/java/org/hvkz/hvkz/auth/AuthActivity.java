package org.hvkz.hvkz.auth;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppActivity;

@Layout(R.layout.activity_login)
public class AuthActivity extends AppActivity<AuthPresenter>
{
    public static final String TAG = "AuthActivity";
    public static final String ACTION_SMS_RECEIVE = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    protected AuthPresenter createPresenter() {
        return new AuthPresenter(this);
    }
}

