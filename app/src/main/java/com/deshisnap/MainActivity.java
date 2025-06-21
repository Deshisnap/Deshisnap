package com.deshisnap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // gradiant colour

        TextView greet_titile = findViewById(R.id.greettitle);
        Utils.applyGradientToText(greet_titile, "#04FDAA", "#01D3F8");

        TextView taglinepart2 = findViewById(R.id.taglinepart2);
        Utils.applyGradientToText(taglinepart2, "#04FDAA", "#01D3F8");

        TextView homemaintain_text = findViewById(R.id.homemaintain_text);
        Utils.applyGradientToText(homemaintain_text, "#04FDAA", "#01D3F8");

        TextView household_service = findViewById(R.id.householdservice);
        Utils.applyGradientToText(household_service, "#04FDAA", "#01D3F8");

        TextView lifestyle_text = findViewById(R.id.lifestyle_text);
        Utils.applyGradientToText(lifestyle_text, "#04FDAA", "#01D3F8");

      //gradiant over

        //recycleviews


        CardAdapter cardAdapter;
        List<CardItem> cardItemList;
        RecyclerView homemaintain = findViewById(R.id.homemaintain_scroll);

        // Set horizontal layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homemaintain.setLayoutManager(layoutManager);

        // Data
        cardItemList = new ArrayList<>();
        cardItemList.add(new CardItem(R.drawable.plumber, "plumber"));
        cardItemList.add(new CardItem(R.drawable.electrician, "electrician"));
        cardItemList.add(new CardItem(R.drawable.homerepair, "homerepair"));
        cardItemList.add(new CardItem(R.drawable.homeservice, "homeservice"));

        // Adapter
        cardAdapter = new CardAdapter(cardItemList);
        homemaintain.setAdapter(cardAdapter);

        //householdservice_scroll

        CardAdapter cardAdapter_house_service;
        List<CardItem> cardItemList_house_service;
        RecyclerView homemaintain_house_service = findViewById(R.id.householdservice_scroll);

        // Set horizontal layout manager
        LinearLayoutManager layoutManager_house_service = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homemaintain_house_service.setLayoutManager(layoutManager_house_service);

        // Data
        cardItemList_house_service = new ArrayList<>();
        cardItemList_house_service.add(new CardItem(R.drawable.plumber, "plumber"));
        cardItemList_house_service.add(new CardItem(R.drawable.electrician, "electrician"));
        cardItemList_house_service.add(new CardItem(R.drawable.homerepair, "homerepair"));
        cardItemList_house_service.add(new CardItem(R.drawable.homeservice, "homeservice"));

        // Adapter
        cardAdapter_house_service = new CardAdapter(cardItemList_house_service);
        homemaintain_house_service.setAdapter(cardAdapter_house_service);

        //lifestyle

        CardAdapter cardAdapter_lifestyle_scroll;
        List<CardItem> cardItemList_lifestyle_scroll;
        RecyclerView homemaintain_lifestyle_scroll = findViewById(R.id.lifestyle_scroll);

        // Set horizontal layout manager
        LinearLayoutManager layoutManager_lifestyle_scroll = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        homemaintain_lifestyle_scroll.setLayoutManager(layoutManager_lifestyle_scroll);

        // Data
        cardItemList_lifestyle_scroll = new ArrayList<>();
        cardItemList_lifestyle_scroll.add(new CardItem(R.drawable.plumber, "plumber"));
        cardItemList_lifestyle_scroll.add(new CardItem(R.drawable.electrician, "electrician"));
        cardItemList_lifestyle_scroll.add(new CardItem(R.drawable.homerepair, "homerepair"));
        cardItemList_lifestyle_scroll.add(new CardItem(R.drawable.homeservice, "homeservice"));

        // Adapter
        cardAdapter_lifestyle_scroll= new CardAdapter(cardItemList_lifestyle_scroll);
        homemaintain_lifestyle_scroll.setAdapter(cardAdapter_lifestyle_scroll);

        //recycle view ends

        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CartPage.class));
        });

        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BookingActivity.class));
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotificationPage.class));
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Profile_page.class));
        });






        //Nav bar




    }
}