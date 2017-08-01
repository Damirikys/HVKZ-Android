package org.hvkz.hvkz.templates;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hvkz.hvkz.uimodels.ViewBinder;

public abstract class UniversalViewHolder<S> extends RecyclerView.ViewHolder
{
    private UniversalAdapter<S> adapter;
    private Context context;
    private S item;

    public UniversalViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        ViewBinder.handle(this, itemView);
    }

    final void hold(S obj) {
        this.item = obj;
        hold();
    }

    public final S item() {
        return item;
    }

    public final Context context() {
        return context;
    }

    void bindAdapter(UniversalAdapter<S> adapter) {
        this.adapter = adapter;
    }

    public final UniversalAdapter<S> getAdapter() {
        return adapter;
    }

    protected abstract void hold();
}
