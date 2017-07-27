package org.hvkz.hvkz.auth;

public interface AuthCallback
{
    void onAuthenticateFailed(String desc);

    void onAuthenticateSuccess();
}
