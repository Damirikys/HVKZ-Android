package org.hvkz.hvkz.interfaces;

import android.app.Activity;
import android.content.Context;

public interface BaseWindow
{
    void showProgress(String message);

    void hideProgress();

    void dialogMessage(String title, String message);

    Context getContext();

    Activity getActivity();
}
