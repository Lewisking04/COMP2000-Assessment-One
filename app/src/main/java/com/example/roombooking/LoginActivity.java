package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Login screen. Demo only: any username with the password demo1234 signs in as an employee. */
public class LoginActivity extends AppCompatActivity {

    private static final String DEMO_PASSWORD = "demo1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputLayout userLayout = findViewById(R.id.user_layout);
        TextInputLayout passLayout = findViewById(R.id.pass_layout);
        TextInputEditText etUser = findViewById(R.id.et_user);
        TextInputEditText etPass = findViewById(R.id.et_pass);
        TextView tvError = findViewById(R.id.tv_login_error);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String user = etUser.getText() == null ? "" : etUser.getText().toString().trim();
            String pass = etPass.getText() == null ? "" : etPass.getText().toString();

            // Clear old messages, then check the fields
            userLayout.setError(null);
            passLayout.setError(null);
            tvError.setVisibility(View.GONE);

            boolean ok = true;
            if (user.isEmpty()) {
                userLayout.setError(getString(R.string.error_username_required));
                ok = false;
            }
            if (pass.isEmpty()) {
                passLayout.setError(getString(R.string.error_password_required));
                ok = false;
            }
            if (!ok) return;

            if (!DEMO_PASSWORD.equals(pass)) {
                tvError.setVisibility(View.VISIBLE);   // "Wrong username or password"
                return;
            }
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
