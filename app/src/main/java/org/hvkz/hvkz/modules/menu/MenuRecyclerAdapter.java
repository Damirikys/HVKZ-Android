package org.hvkz.hvkz.modules.menu;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.firebase.db.MenuStorage;

import java.util.List;

class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuItemViewHolder>
{
    private List<MenuStorage.MenuItem> menuItems;

    MenuRecyclerAdapter(List<MenuStorage.MenuItem> items) {
        this.menuItems = items;
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
