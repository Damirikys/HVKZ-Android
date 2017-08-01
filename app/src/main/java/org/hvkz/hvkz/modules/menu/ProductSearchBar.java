package org.hvkz.hvkz.modules.menu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.lang.reflect.Field;

public class ProductSearchBar extends MaterialSearchBar
{
    private OnSearchActionListener onSearchActionListener;

    public ProductSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProductSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public ProductSearchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setOnSearchActionListener(OnSearchActionListener onSearchActionListener) {
        super.setOnSearchActionListener(onSearchActionListener);
        this.onSearchActionListener = onSearchActionListener;
    }

    public boolean isListenerAttached() {
        return onSearchActionListener != null;
    }

    public boolean isSuggestionsVisible() {
        try {
            Field field = getClass().getSuperclass().getDeclaredField("suggestionsVisible");
            field.setAccessible(true);
            return (boolean) field.get(this);
        } catch (Exception e) {
            return false;
        }
    }

    public EditText getSearchEdit() {
        try {
            Field field = getClass().getSuperclass().getDeclaredField("searchEdit");
            field.setAccessible(true);
            return (EditText) field.get(this);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (isListenerAttached())
            onSearchActionListener.onSearchConfirmed(getSearchEdit().getText());
        return true;
    }
}
