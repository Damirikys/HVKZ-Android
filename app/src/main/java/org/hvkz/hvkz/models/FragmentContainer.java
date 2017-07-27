package org.hvkz.hvkz.models;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;

@Layout(R.layout.fragment_container)
public class FragmentContainer extends AppFragment<Router>
{
    private Router router;

    private FragmentContainer bindRouter(Router router) {
        this.router = router;
        return this;
    }

    @Override
    protected Router bindPresenter() {
        return router.handleParent(this);
    }

    public static FragmentContainer with(Router router) {
        return AppFragment.instanceOf(FragmentContainer.class).bindRouter(router);
    }
}
