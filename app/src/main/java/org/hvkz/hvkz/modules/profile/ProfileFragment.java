package org.hvkz.hvkz.modules.profile;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.uimodels.AppFragment;

@Layout(R.layout.fragment_profile)
public class ProfileFragment extends AppFragment<ProfilePresenter>
{
    public static final String USER_ID = "org.hvkz.USER_ID";

    @Override
    protected ProfilePresenter bindPresenter() {
        return new ProfilePresenter(this);
    }
}
