package com.deshisnap;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deshisnap.OtpPage;
import com.deshisnap.R;
import com.deshisnap.Registration_page;
import com.deshisnap.Utils;

import java.util.ArrayList;
import java.util.List;

public class loginPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Find the TextView
        TextView login_page_heading= findViewById(R.id.login_page_heading);
        Utils.applyGradientToText(login_page_heading, "#FFFFFF", "#FFFFFF");

        findViewById(R.id.send_otp).setOnClickListener(v -> {
            startActivity(new Intent(loginPage.this, OtpPage.class));
            finish();
        });

        findViewById(R.id.google_login_button).setOnClickListener(v -> {
            startActivity(new Intent(loginPage.this, Registration_page.class));
            finish();
        });







    }
}


