package org.hvkz.hvkz.models;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.SparseArray;

import org.hvkz.hvkz.interfaces.BaseActivity;
import org.hvkz.hvkz.interfaces.Destroyable;

public final class ViewModel<T extends BaseActivity> implements Destroyable
{
    private T model;
    private SparseArray<android.view.View> viewMap;

    public ViewModel(T model) {
        this.model = model;
        this.viewMap = new SparseArray<>();
    }

    public Context context() {
        return model.getActivity();
    }

    public <V extends android.view.View> ViewWrapper<V> on(Class<V> tClass) {
        return new ViewWrapper<V>();
    }

    @SuppressWarnings("unchecked")
    public class ViewWrapper<V extends android.view.View> {
        public V with(@IdRes int resId) {
            android.view.View extracted = viewMap.get(resId);
            if (extracted != null) {
                return (V) extracted;
            } else {
                android.view.View view = model.getActivity().findViewById(resId);
                viewMap.put(resId, model.getActivity().findViewById(resId));
                return (V) view;
            }
        }
    }

    @Override
    public void onDestroy() {
        viewMap.clear();
        model = null;
        viewMap = null;
    }
}
