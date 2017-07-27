package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.utils.ContextApp;

import java.util.List;

public class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuItemViewHolder>
{
    private List<MenuStorage.MenuItem> menuItems;

    public MenuRecyclerAdapter(Context context) {
        this.menuItems = ContextApp.getApp(context)
                .getMenuStorage()
                .getWeeklyMenu();
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_view, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder holder, int position) {
        holder.hold(menuItems.get(position));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }
}
