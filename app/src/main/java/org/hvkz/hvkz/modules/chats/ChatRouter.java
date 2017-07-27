package org.hvkz.hvkz.modules.chats;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.models.Router;
import org.hvkz.hvkz.modules.RouteChannel;
import org.hvkz.hvkz.modules.chats.window.ChatWindowFragment;

public class ChatRouter extends Router
{
    public static final String DOMAIN_KEY = "org.hvkz.chats.DOMAIN_KEY";
    public static final String CHAT_TYPE_KEY = "org.hvkz.chats.CHAT_TYPE_KEY";

    @Override
    public void onRouteRequest(RouteChannel.RouteRequest request) {
        Bundle args = request.getArgs();
        if (args.getString(DOMAIN_KEY) != null) {
            moveToChat((ChatType) args.getSerializable(CHAT_TYPE_KEY), args.getString(DOMAIN_KEY));
        }
    }

    @Override
    protected FragmentTransaction getBaseTransaction(FragmentTransaction transaction) {
        return transaction.add(R.id.fragmentContainer, AppFragment.instanceOf(ChatsFragment.class));
    }

    public void moveToChat(ChatType chatType, String domain) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHAT_TYPE_KEY, chatType);
        bundle.putString(DOMAIN_KEY, domain);

        ChatWindowFragment chatWindowFragment = AppFragment.instanceOf(ChatWindowFragment.class);
        chatWindowFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(containerId, chatWindowFragment)
                .addToBackStack(null)
                .commit();

        getFragmentContainer()
                .getActivity()
                .findViewById(R.id.navigation)
                .setVisibility(View.GONE);
    }

    @Override
    public void onResultReceive(int requestCode, int resultCode, Intent dataIntent) {
        try {
            AppFragment.of(getFragmentManager().findFragmentById(containerId))
                    .getPresenter()
                    .onResultReceive(requestCode, resultCode, dataIntent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
