package org.hvkz.hvkz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.View;
import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.HVKZApp;
import org.hvkz.hvkz.sync.SyncPresenter;

import javax.inject.Inject;

@Layout(R.layout.activity_main)
public class MainActivity extends AppActivity<SyncPresenter>
{
    @Inject
    FirebaseUser currentUser;

    @View(R.id.message)
    TextView mTextMessage;

    @View(R.id.navigation)
    BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    Toast.makeText(MainActivity.this, currentUser.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    FirebaseAuth.getInstance().signOut();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        HVKZApp.component().inject(this);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected SyncPresenter createPresenter() {
        return new SyncPresenter(this);
    }
}
