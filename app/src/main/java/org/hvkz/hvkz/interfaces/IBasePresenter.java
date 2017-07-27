package org.hvkz.hvkz.interfaces;

import android.content.Intent;

public interface IBasePresenter extends Destroyable
{
    void init();
    void onResultReceive(int requestCode, int resultCode, Intent dataIntent);
}
