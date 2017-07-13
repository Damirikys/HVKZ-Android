package org.hvkz.hvkz.modules.chats;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.models.AppFragment;
import org.hvkz.hvkz.models.Router;

public class ChatRouter extends Router
{
    @Override
    protected FragmentTransaction getBaseTransaction(FragmentTransaction transaction) {
        return transaction.add(R.id.fragmentContainer, AppFragment.instanceOf(ChatsFragment.class));
    }

    public void moveToChat(String address) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, AppFragment.instanceOf(Fragment.class))
                .addToBackStack(null)
                .commit();
    }
}
