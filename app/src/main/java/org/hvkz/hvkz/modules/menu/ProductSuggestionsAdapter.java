package org.hvkz.hvkz.modules.menu;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.uimodels.ViewBinder;

import java.util.ArrayList;
import java.util.List;

import static com.mancj.materialsearchbar.MaterialSearchBar.BUTTON_NAVIGATION;
import static com.mancj.materialsearchbar.MaterialSearchBar.BUTTON_SPEECH;

class ProductSuggestionsAdapter extends SuggestionsAdapter<MenuStorage.Product, ProductSuggestionsAdapter.SuggestionHolder>
    implements MaterialSearchBar.OnSearchActionListener, TextWatcher
{
    private List<MenuStorage.Product> catalog;
    private ProductSearchBar searchBar;

    ProductSuggestionsAdapter(ProductSearchBar searchBar, List<MenuStorage.Product> catalog) {
        super(LayoutInflater.from(searchBar.getContext()));
        this.searchBar = searchBar;
        this.catalog = catalog;
        this.searchBar.addTextChangeListener(this);
        setSuggestions(catalog);
    }

    @Override
    public void onBindSuggestionHolder(MenuStorage.Product suggestion, SuggestionHolder holder, int position) {
        holder.hold(suggestion);
    }

    @Override
    public int getSingleViewHeight() {
        return 77;
    }

    @Override
    public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.product_suggestion_item, parent, false);
        return new SuggestionHolder(view);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled) setSuggestions(catalog);
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        if (text.toString().isEmpty()) {
            setSuggestions(catalog);
            return;
        }

        List<MenuStorage.Product> result = new ArrayList<>();
        for (MenuStorage.Product product : catalog) {
            if (product.product.toLowerCase().contains(text.toString().toLowerCase())) {
                result.add(product);
            }
        }

        setSuggestions(result);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case BUTTON_NAVIGATION:
                if (!searchBar.isSuggestionsVisible()) {
                    searchBar.enableSearch();
                } else {
                    searchBar.disableSearch();
                }

                break;
            case BUTTON_SPEECH:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        onSearchConfirmed(s);
    }

    static class SuggestionHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.product)
        private TextView productView;

        @BindView(R.id.calories)
        private TextView caloriesView;

        SuggestionHolder(View itemView) {
            super(itemView);
            ViewBinder.handle(this, itemView);
        }

        public void hold(MenuStorage.Product product) {
            productView.setText(product.product);
            caloriesView.setText(product.calories);
        }
    }
}
