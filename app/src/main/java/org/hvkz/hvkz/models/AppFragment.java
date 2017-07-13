package org.hvkz.hvkz.models;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.Destroyable;

@SuppressWarnings("unchecked")
public abstract class AppFragment<T extends Destroyable> extends Fragment implements BaseWindow
{
    private BaseWindow parent;
    private T presenter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Class<? extends AppFragment> clazz = getClass();
        View layout = inflater.inflate(clazz.getAnnotation(Layout.class).value(), container, false);
        ViewBinder.handle(this, layout);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent = (BaseWindow) getActivity();
        presenter = bindPresenter();
    }

    public static <T extends Fragment> T instanceOf(Class<T> tClass) {
        try {
          return tClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract T bindPresenter();

    public T getPresenter() {
        if (presenter == null) {
            presenter = bindPresenter();
        }

        return presenter;
    }

    protected BaseWindow getParentActivity() {
        return parent;
    }

    @Override
    public void showProgress(String message) {
        getParentActivity().showProgress(message);
    }

    @Override
    public void hideProgress() {
        getParentActivity().hideProgress();
    }

    @Override
    public void dialogMessage(String title, String message) {
        getParentActivity().dialogMessage(title, message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy();
    }
}
