package org.hvkz.hvkz.xmpp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.hvkz.hvkz.xmpp.ConnectionService;

public class XMPPBootBroadcast extends BroadcastReceiver
{
    public static final String BROADCAST_KEY = XMPPBootBroadcast.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                context.startService(new Intent(context, ConnectionService.class));
                break;
            default:
                Intent i = new Intent(context, ConnectionService.class);
                i.putExtra(BROADCAST_KEY, true);
                context.startService(i);
                break;
        }
    }
}