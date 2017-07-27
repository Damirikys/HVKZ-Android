package org.hvkz.hvkz.modules.chats.window;

import android.app.Dialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hvkz.hvkz.interfaces.Destroyable;
import org.hvkz.hvkz.uapi.models.entities.User;

import java.util.List;

import static org.hvkz.hvkz.utils.Tools.dpToPx;

public class MembersListWrapper implements Destroyable
{
    private Dialog dialog;
    private RecyclerView recyclerView;

    private MembersListWrapper(Dialog dialog, List<User> users) {
        this.dialog = dialog;
        this.recyclerView = new RecyclerView(dialog.getContext());
        this.recyclerView.setLayoutManager(new LinearLayoutManager(dialog.getContext()));
        this.recyclerView.setAdapter(new MembersListAdapter(this, users));
        this.recyclerView.setPadding(0, dpToPx(dialog.getContext().getResources().getDisplayMetrics(), 16), 0, 0);
    }

    @Override
    public void onDestroy() {
        dialog.dismiss();
    }

    private View getView() {
        return recyclerView;
    }

    public static View inflate(Dialog dialog, List<User> users) {
        return new MembersListWrapper(dialog, users)
                .getView();
    }
}
