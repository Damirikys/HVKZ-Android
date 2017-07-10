package org.hvkz.hvkz.sync;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;

import org.hvkz.hvkz.R;
import org.hvkz.hvkz.adapters.TextWatcherAdapter;
import org.hvkz.hvkz.annotations.BindView;
import org.hvkz.hvkz.annotations.Layout;
import org.hvkz.hvkz.annotations.OnClick;
import org.hvkz.hvkz.models.AppActivity;
import org.hvkz.hvkz.utils.validators.EmailValidator;

@Layout(R.layout.activity_settings)
public class SyncActivity extends AppActivity<SyncPresenter>
{
    @BindView(R.id.email_edit_text)
    EditText emailEditText;
    @BindView(R.id.email_confirm_button)
    Button emailConfirmButton;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        emailEditText.addTextChangedListener(new TextWatcherAdapter()
        {
            @Override
            public void afterTextChanged(Editable s) {
                emailConfirmButton.setEnabled(EmailValidator.emailAddressIsCorrect(s.toString()));
            }
        });
    }

    @OnClick(R.id.email_confirm_button)
    public void onEmailConfirm() {
        if (EmailValidator.emailAddressIsCorrect(emailEditText.getText().toString())) {
            showProgress("Подождите...");
            getPresenter().startSync(emailEditText.getText().toString());
        }
    }

    @Override
    protected SyncPresenter createPresenter() {
        return new SyncPresenter(this);
    }
}
