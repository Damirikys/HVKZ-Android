package org.hvkz.hvkz.app;

import android.app.Activity;
import android.content.Context;

public interface BaseActivity
{
    void showProgress(String message);

    void hideProgress();

    void dialogMessage(String title, String message);

    Context getContext();

    Activity getActivity();
}
