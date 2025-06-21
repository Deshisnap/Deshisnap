package com.foodcafe.myapplication;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class Registration_page extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);

        // Find the TextView
        TextView login_page_heading= findViewById(R.id.registration_page_heading);
        Utils.applyGradientToText(login_page_heading, "#FFFFFF", "#FFFFFF");







    }
}


