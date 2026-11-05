package com.example.roombooking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Login screen. Demo only: any username with the password demo1234 signs in as an employee. */
public class LoginActivity extends AppCompatActivity {

    private static final String DEMO_PASSWORD = "demo1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUser = findViewById(R.id.et_user);
        EditText etPass = findViewById(R.id.et_pass);
        TextView tvError = findViewById(R.id.tv_login_error);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString().trim();
                String pass = etPass.getText().toString();

                // Clear old messages first
                etUser.setError(null);
                etPass.setError(null);
                tvError.setVisibility(View.GONE);

                boolean ok = true;
                if (user.isEmpty()) {
                    etUser.setError(getString(R.string.error_username_required));
                    ok = false;
                }
                if (pass.isEmpty()) {
                    etPass.setError(getString(R.string.error_password_required));
                    ok = false;
                }
                if (!ok) return;

                if (!DEMO_PASSWORD.equals(pass)) {
                    tvError.setVisibility(View.VISIBLE);   // "Wrong username or password"
                    return;
                }
                // Open the home screen, then close the login screen so Back does not return to it
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
