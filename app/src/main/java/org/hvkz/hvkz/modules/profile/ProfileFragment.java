package org.hvkz.hvkz.modules.profile;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppFragment;

@Layout(R.layout.fragment_profile)
public class ProfileFragment extends AppFragment<ProfilePresenter>
{
    @Override
    protected ProfilePresenter bindPresenter() {
        return new ProfilePresenter(this);
    }
}
