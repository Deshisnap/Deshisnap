package com.deshisnap;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.deshisnap.Utils;


public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        
        // gradiant colour
        TextView company_title= findViewById(R.id.company_title);
        Utils.applyGradientToText(company_title, "#04FDAA", "#01D3F8");


        TextView company_location = findViewById(R.id.company_location);
        Utils.applyGradientToText(company_location, "#04FDAA", "#01D3F8");


        TextView company_desc = findViewById(R.id.company_desc);
        Utils.applyGradientToText(company_desc, "#04FDAA", "#01D3F8");

        TextView company_supportgmail = findViewById(R.id.company_supportgmail);
        Utils.applyGradientToText(company_supportgmail, "#04FDAA", "#01D3F8");

             // ✅ Back button text
        ImageView backText = findViewById(R.id.back_button);
        backText.setOnClickListener(v -> {
            onBackPressed(); // Go back to previous activity
        }); // Go back to previous activity
        }

       
    
}
