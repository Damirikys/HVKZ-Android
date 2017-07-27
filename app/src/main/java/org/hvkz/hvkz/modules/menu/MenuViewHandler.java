package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.ViewHandler;

public class MenuViewHandler extends ViewHandler<MenuPresenter>
{
    @BindView(R.id.recyclerMenuView)
    private RecyclerView recyclerView;

    public MenuViewHandler(BaseWindow<MenuPresenter> baseWindow) {
        super(baseWindow);
    }

    @Override
    protected void handle(Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new MenuRecyclerAdapter(context));
    }
}
