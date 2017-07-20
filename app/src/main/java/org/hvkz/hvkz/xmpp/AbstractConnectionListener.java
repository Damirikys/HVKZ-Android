package org.hvkz.hvkz.xmpp;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public abstract class AbstractConnectionListener implements ConnectionListener
{
    private static final String TAG = "ConnectionListener";

    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TAG, "Connected!");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "Authenticated!");
    }

    @Override
    public void connectionClosed() {
        Log.d(TAG, "Connection closed!");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TAG, "Connection aborted!");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(TAG, "Recconection successful!");
    }

    @Override
    public void reconnectingIn(int seconds) {
        // Stub!
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(TAG, "Recconection failed.");
    }
}