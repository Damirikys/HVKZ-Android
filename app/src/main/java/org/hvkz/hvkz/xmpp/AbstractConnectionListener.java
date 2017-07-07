package org.hvkz.hvkz.xmpp;

import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

public abstract class AbstractConnectionListener implements ConnectionListener
{
    private static final String TAG = "ConnectionListener";

    @Override
    public void connected(XMPPConnection connection) {
        Log.d(TAG, "CONNECTED!");
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d(TAG, "AUTHENTICATED!");
    }

    @Override
    public void connectionClosed() {
        Log.d(TAG, "CONNECTION CLOSED!");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.d(TAG, "CONNECTION ABORTED!");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.d(TAG, "RECONNECTION SUCCESSFUL!");
    }

    @Override
    public void reconnectingIn(int seconds) {
        // Stub!
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.d(TAG, "RECONNECTION FAILED!");
    }
}