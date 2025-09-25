package com.deshisnap;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView title = findViewById(R.id.about_title);
        TextView body = findViewById(R.id.about_body);

        title.setText("About DeshiSnap");
        body.setText("DeshiSnap is your trusted home services partner. We connect you with verified professionals across plumbing, electrical, cleaning, and more — quickly and reliably.\n\nOur mission is to simplify your daily life with transparent pricing, quality service, and easy bookings.\n\nContact us: deshisnap247service@gmail.com");
    }
}
