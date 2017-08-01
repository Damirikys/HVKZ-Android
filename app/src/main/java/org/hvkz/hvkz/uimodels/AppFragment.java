package org.hvkz.hvkz.uimodels;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.interfaces.BaseWindow;
import org.hvkz.hvkz.interfaces.IBasePresenter;

@SuppressWarnings("unchecked")
public abstract class AppFragment<T extends IBasePresenter> extends DialogFragment implements BaseWindow<T>
{
    private BaseWindow<T> parent;
    private IBasePresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

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
        parent = (BaseWindow<T>) getActivity();
        presenter = bindPresenter();
        presenter.init();
    }

    protected abstract T bindPresenter();

    public T getPresenter() {
        if (presenter == null) presenter = bindPresenter();
        return (T) presenter;
    }

    @Override
    public void onBackPressed() {
        getParentActivity().onBackPressed();
    }

    protected BaseWindow<T> getParentActivity() {
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
    public Object getViewFinder() {
        return getView();
    }

    public static <T extends Fragment> T instanceOf(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static AppFragment<IBasePresenter> of(Fragment fragment) {
        return (AppFragment<IBasePresenter>) fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.onDestroy();
        parent = null;
        presenter = null;
    }
}
