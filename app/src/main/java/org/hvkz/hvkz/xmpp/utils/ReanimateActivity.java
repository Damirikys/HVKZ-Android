package org.hvkz.hvkz.xmpp.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.hvkz.hvkz.xmpp.ConnectionService;

public class ReanimateActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ConnectionService.class));
        finish();
    }
}
