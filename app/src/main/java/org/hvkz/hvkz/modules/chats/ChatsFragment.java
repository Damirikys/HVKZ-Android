package org.hvkz.hvkz.modules.chats;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.modules.RouteChannel;

@Layout(R.layout.fragment_chats_list)
public class ChatsFragment extends AppFragment<ChatsPresenter>
{
    private boolean routeExtracted;

    @Override
    protected ChatsPresenter bindPresenter() {
        return new ChatsPresenter(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        routeIfNeeded();
    }

    private void routeIfNeeded() {
        getActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);

        Bundle routeBundle = getParentActivity().getActivity().getIntent().getBundleExtra("route");
        if (routeBundle != null && !routeExtracted) {
            RouteChannel.sendRouteRequest(new RouteChannel.RouteRequest(routeBundle));
            routeExtracted = true;
        }
    }
}
