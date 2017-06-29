package org.hvkz.hvkz.auth;

import org.hvkz.hvkz.interfaces.ActivityWrapper;

public interface AuthCallback extends ActivityWrapper
{
    void onAuthenticateFailed(String desc);

    void onAuthenticateSuccess();
}
