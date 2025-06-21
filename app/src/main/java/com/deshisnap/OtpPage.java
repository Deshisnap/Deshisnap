package com.deshisnap;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class OtpPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_page);

        // Find the TextView
        TextView notification_page_heading= findViewById(R.id.textView4);
        Utils.applyGradientToText(notification_page_heading, "#04FDAA", "#01D3F8");







    }
}


