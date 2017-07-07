package org.hvkz.hvkz.modules.home;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.View;
import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.interfaces.ActivityWrapper;
import org.hvkz.hvkz.xmpp.AbstractConnectionListener;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.LocalBinder;
import org.jivesoftware.smack.XMPPConnection;

@Layout(R.layout.activity_main)
public class MainActivity extends AppActivity<MainPresenter> implements ActivityWrapper
{
    private static final String TAG = "MainActivity";

    @View(R.id.navigation)
    BottomNavigationView navigation;

    final int GALLERY_REQUEST = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                return true;
            case R.id.navigation_dashboard:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                return true;
            case R.id.navigation_notifications:
                return true;
        }
        return false;
    };

    private Intent serviceIntent;
    private LocalBinder<ConnectionService> serviceBinder;

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            serviceBinder = (LocalBinder<ConnectionService>) binder;
            lolkek();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    private void lolkek() {
        ConnectionService connectionService = serviceBinder.getService();
        System.out.println("ISCONNECTED: " + connectionService.getConnection().isConnected());
        System.out.println("ISAUTH: " + connectionService.getConnection().isAuthenticated());
        connectionService.getConnection().addConnectionListener(new AbstractConnectionListener()
        {
            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                super.authenticated(connection, resumed);
                Log.d(TAG, "АУТЕНТИФИЦИРОВАНО !!! ДЕЛАЕМ ЧЕ ХОТИМ!!!");
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HVKZApp.component().inject(this);
        startService(serviceIntent =  new Intent(this, ConnectionService.class));

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(serviceIntent, mServerConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent)
    {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            LinearLayout layout = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(params);

            layout.setGravity(Gravity.CLIP_VERTICAL);
            layout.setPadding(10, 10, 10, 10);

            TextView tv = new TextView(this);
            tv.setText("Добавление описание");
            tv.setPadding(30, 30, 20, 20);
            tv.setGravity(Gravity.LEFT);
            tv.setTextSize(17);

            EditText et = new EditText(this);

            LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv1Params.setMargins(20, 40, 20, 20);
            layout.addView(et, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            alertDialogBuilder.setView(layout);
            alertDialogBuilder.setCustomTitle(tv);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setNegativeButton("Отменить", (dialog, whichButton) -> dialog.cancel());

            alertDialogBuilder
                    .setPositiveButton("Загрузить", (dialog, which) ->
                    {
                        ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setMessage("Загрузка фотографии...");
                        progressDialog.show();

                        getPresenter().uploadImage(imageReturnedIntent.getData(), et.getText().toString());
                    });

            alertDialogBuilder.create().show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServerConn);
    }

    @Override
    protected MainPresenter createPresenter() {
        return new MainPresenter(this);
    }
}
