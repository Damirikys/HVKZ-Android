package org.hvkz.hvkz.templates;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.annotations.Layout;

import java.util.List;

public class UniversalAdapter<S> extends RecyclerView.Adapter<UniversalViewHolder<S>>
{
    private final List<S> defaultData;

    private VHolderExtractor<S> extractor;
    protected List<S> data;

    private Handler handler;

    public UniversalAdapter(List<S> data, VHolderExtractor<S> extractor) {
        this.defaultData = data;
        this.data = data;
        this.extractor = extractor;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public UniversalViewHolder<S> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(extractor.getLayoutRes(), parent, false);
        UniversalViewHolder<S> holder = extractor.extract(view);
        holder.bindAdapter(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(UniversalViewHolder<S> holder, int position) {
        holder.hold(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<S> getDefaultData() {
        return defaultData;
    }

    public void updateData(List<S> data) {
        this.data = data;
        postUI(this::notifyDataSetChanged);
    }

    public void backupData() {
        this.data = defaultData;
        postUI(this::notifyDataSetChanged);
    }

    private void postUI(Runnable runnable) {
        handler.post(runnable);
    }

    @Layout
    public static abstract class VHolderExtractor<S>
    {
        private int layoutRes;

        protected VHolderExtractor() {
            if (getClass().isAnnotationPresent(Layout.class))
                this.layoutRes = getClass().getAnnotation(Layout.class).value();
        }

        public abstract UniversalViewHolder<S> extract(View view);

        int getLayoutRes() {
            return layoutRes;
        }
    }
}
