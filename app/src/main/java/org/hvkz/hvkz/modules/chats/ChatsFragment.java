package org.hvkz.hvkz.modules.chats;

import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.models.AppFragment;

@Layout(R.layout.fragment_chats_list)
public class ChatsFragment extends AppFragment<ChatsPresenter>
{
    private View navigationView;

    @Override
    protected ChatsPresenter bindPresenter() {
        return new ChatsPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView = getActivity().findViewById(R.id.navigation);
        navigationView.setVisibility(View.VISIBLE);
    }
}
