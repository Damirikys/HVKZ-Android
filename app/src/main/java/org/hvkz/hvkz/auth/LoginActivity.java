package org.hvkz.hvkz.auth;

import android.widget.EditText;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.app.AppActivity;
import org.hvkz.hvkz.app.annotations.Layout;
import org.hvkz.hvkz.app.annotations.OnClick;
import org.hvkz.hvkz.app.annotations.View;

@Layout(R.layout.activity_login)
public class LoginActivity extends AppActivity<LoginPresenter>
{
    @View(R.id.phone_edit_text)
    EditText phoneEditText;
    @View(R.id.password_edit_text)
    EditText passwordEditText;

    @OnClick(R.id.sign_in_button)
    public void signIn() {
        String username = phoneEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        getPresenter().login(username, password);
    }

    @Override
    protected LoginPresenter createPresenter() {
        return new LoginPresenter();
    }
}

