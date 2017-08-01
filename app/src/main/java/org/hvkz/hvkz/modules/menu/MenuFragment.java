package org.hvkz.hvkz.modules.menu;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.uimodels.AppFragment;

@Layout(R.layout.fragment_menu)
public class MenuFragment extends AppFragment<MenuPresenter>
{
    @Override
    protected MenuPresenter bindPresenter() {
        return new MenuPresenter(this);
    }
}
