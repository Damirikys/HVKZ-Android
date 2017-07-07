package org.hvkz.hvkz.modules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import org.hvkz.hvkz.R;

public class Main2Activity extends AppCompatActivity
{

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        webView = (WebView) findViewById(R.id.webview);
        webView.loadUrl("http://hvkz.org/pda/photo/0-0-0-1");
    }
}
