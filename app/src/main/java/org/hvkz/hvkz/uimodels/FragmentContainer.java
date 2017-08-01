package org.hvkz.hvkz.uimodels;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.EventReceiver;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.event.Event;
import org.hvkz.hvkz.event.EventChannel;
import org.hvkz.hvkz.modules.profile.ProfileFragment;
import org.hvkz.hvkz.router.Router;
import org.hvkz.hvkz.uapi.User;

import static org.hvkz.hvkz.modules.profile.ProfileFragment.USER_ID;

@Layout(R.layout.fragment_container)
public class FragmentContainer extends AppFragment<Router>
{
    private Router router;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventChannel.connect(this);
    }

    @EventReceiver
    public void onEventReceive(Event<User> event) {
        if (event.getType() == Event.EventType.USER_PROFILE_OPEN) {
            Bundle bundle = new Bundle();
            bundle.putInt(USER_ID, event.getData().getUserId());

            ProfileFragment fragment = AppFragment.instanceOf(ProfileFragment.class);
            fragment.setArguments(bundle);
            fragment.show(getChildFragmentManager(), Event.EventType.USER_PROFILE_OPEN.name());
        }
    }

    private FragmentContainer bindRouter(Router router) {
        this.router = router;
        return this;
    }

    @Override
    protected Router bindPresenter() {
        return router.handleParent(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventChannel.disconnect(this);
    }

    public static FragmentContainer with(Router router) {
        return AppFragment.instanceOf(FragmentContainer.class).bindRouter(router);
    }
}
