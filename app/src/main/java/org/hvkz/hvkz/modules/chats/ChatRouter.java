package org.hvkz.hvkz.modules.chats;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.models.Router;
import org.hvkz.hvkz.modules.chats.window.ChatWindowFragment;

public class ChatRouter extends Router
{
    public static final String DOMAIN_KEY = "org.hvkz.chats.DOMAIN_KEY";
    public static final String CHAT_TYPE_KEY = "org.hvkz.chats.CHAT_TYPE_KEY";

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
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(containerId, chatWindowFragment)
                .addToBackStack(null)
                .commit();
    }
}
