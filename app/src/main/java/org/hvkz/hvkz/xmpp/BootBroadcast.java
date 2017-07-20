package org.hvkz.hvkz.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootBroadcast extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_LONG).show();
        startLogin(context);
    }

    public void startLogin(Context context) {
        context.startService(new Intent(context, ConnectionService.class));
    }
}