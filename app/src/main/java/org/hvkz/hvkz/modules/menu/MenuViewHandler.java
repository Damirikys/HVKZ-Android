package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.templates.ViewHandler;
import org.hvkz.hvkz.utils.ContextApp;

import javax.inject.Inject;

public class MenuViewHandler extends ViewHandler<MenuPresenter>
{
    @BindView(R.id.recyclerMenuView)
    private RecyclerView recyclerView;

    @BindView(R.id.searchBar)
    private ProductSearchBar searchBar;

    @SuppressWarnings("WeakerAccess")
    @Inject
    MenuStorage menuStorage;

    MenuViewHandler(BaseWindow<MenuPresenter> baseWindow) {
        super(baseWindow);
    }

    @Override
    protected void handle(Context context) {
        ContextApp.getApp(context).component().inject(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        searchBar.setCardViewElevation(4);

        menuStorage.getWeeklyMenu(menuItems ->
                postUI(() -> recyclerView.setAdapter(new MenuRecyclerAdapter(menuItems))));

        menuStorage.getProductCatalog(catalog -> postUI(() -> {
            ProductSuggestionsAdapter adapter = new ProductSuggestionsAdapter(searchBar, catalog);
            searchBar.setCustomSuggestionAdapter(adapter);
            searchBar.setOnSearchActionListener(adapter);
        }));
    }
}
