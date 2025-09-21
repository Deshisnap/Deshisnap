package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String ADMIN_EMAIL = "deshisnap@gmail.com";
    private static final String ADMIN_PASSWORD = "kanha+rupali@12239603";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button adminLoginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login_page);

        emailEditText = findViewById(R.id.admin_email_edit_text);
        passwordEditText = findViewById(R.id.admin_password_edit_text);
        adminLoginButton = findViewById(R.id.admin_login_button);

        // Optional: set input types explicitly
        emailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAdminLogin();
            }
        });
    }

    private void attemptAdminLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        if (ADMIN_EMAIL.equalsIgnoreCase(email) && ADMIN_PASSWORD.equals(password)) {
            Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminLoginActivity.this, admin_dashboard.class);
            // Clear back stack so user can't navigate back to login accidentally
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_LONG).show();
        }
    }
}
