package com.example.comp2000roombookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Login screen. Demo only: the password is demo1234. The username manager opens the manager screens,
 * anything else the employee screens.
 */
public class LoginActivity extends AppCompatActivity {

    // Demo only. A real app would never keep a password in the code; Assessment 2 adds proper sign-in.
    private static final String DEMO_PASSWORD = "demo1234";
    // This username is the manager account, every other one is an employee
    private static final String MANAGER_USERNAME = "manager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show activity_login.xml (or the layout-land version when the phone is sideways)
        setContentView(R.layout.activity_login);

        // Connect the views in the XML to Java variables.
        EditText etUser = findViewById(R.id.et_user);
        EditText etPass = findViewById(R.id.et_pass);
        TextView tvError = findViewById(R.id.tv_login_error);
        Button btnLogin = findViewById(R.id.btn_login);

        // Everything inside onClick runs each time the Login button is tapped.
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString().trim();
                String pass = etPass.getText().toString();

                // Clear old messages first
                etUser.setError(null);
                etPass.setError(null);
                tvError.setVisibility(View.GONE);

                // Check every field and show a message under each empty one
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

                // Compare with the demo password. Any username is accepted.
                if (!DEMO_PASSWORD.equals(pass)) {
                    tvError.setVisibility(View.VISIBLE);   // "Wrong username or password"
                    return;
                }

                // Each role goes to its own home screen. An explicit Intent
                // names the exact screen to open.
                Intent next;
                if (user.equalsIgnoreCase(MANAGER_USERNAME)) {
                    next = new Intent(LoginActivity.this, ManagerActivity.class);
                } else {
                    next = new Intent(LoginActivity.this, MainActivity.class);
                }
                startActivity(next);
                finish();     // closes this screen so Back from the home screen does not return to Login
            }
        });
    }
}
