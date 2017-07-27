package org.hvkz.hvkz.interfaces;

import android.app.Activity;
import android.content.Context;

public interface BaseWindow<T>
{
    void showProgress(String message);

    void hideProgress();

    void dialogMessage(String title, String message);

    void onBackPressed();

    T getPresenter();

    Context getContext();

    Activity getActivity();
}
