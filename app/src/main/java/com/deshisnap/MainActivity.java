
package com.deshisnap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager; // Import this
import androidx.recyclerview.widget.RecyclerView;

import com.deshisnap.Booking_page.BookingsStatusDetails;
import com.deshisnap.cart_page.CardAdapter;
import com.deshisnap.service_related_work.Service;

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

        // --- Home Maintenance & Repair Services ---
        RecyclerView homemaintain = findViewById(R.id.homemaintain_scroll);
        // THIS IS WHERE IT WAS MISSING FOR homemaintain_scroll
        homemaintain.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Service> homeMaintenanceServices = new ArrayList<>();

        // Plumber Data
        homeMaintenanceServices.add(new Service(
                R.drawable.plumber,
                "Plumber",
                "Reliable and certified plumbing solutions for your home and office.",
                "Our expert plumbers are available 24/7 to handle all types of plumbing emergencies and routine maintenance. From fixing leaky pipes and clogged drains to installing new fixtures and water heaters, we provide comprehensive services with a focus on quality and customer satisfaction. All our technicians are background-checked and fully insured for your peace of mind. We use only high-quality materials and offer transparent pricing with no hidden fees.",
                "₹ 500 onwards",
                "4.8 (250 reviews)",
                "- Leak detection & repair\n- Drain cleaning & unclogging\n- Faucet & toilet installation\n- Water heater repair & installation\n- Pipe fitting & replacement"
        ));

        // Electrician Data
        homeMaintenanceServices.add(new Service(
                R.drawable.electrician,
                "Electrician",
                "Safe and efficient electrical repairs and installations.",
                "Certified electricians for all your residential and commercial electrical needs. Services include wiring, fuse box repair, new outlet installation, lighting solutions, and fault detection. We ensure adherence to safety standards and provide prompt, reliable service. Emergency call-outs available.",
                "₹ 400 onwards",
                "4.7 (180 reviews)",
                "- Wiring & rewiring\n- Fuse/circuit breaker repair\n- Outlet & switch installation\n- Lighting solutions\n- Electrical inspections"
        ));

        // Painter Data
        homeMaintenanceServices.add(new Service(
                R.drawable.homerepair, // Assuming homerepair is your painter image
                "Painter",
                "Professional painting services for a fresh new look.",
                "Transform your space with our professional painting services. We offer interior and exterior painting, wall preparation, texture painting, and custom color matching. Our experienced painters ensure a smooth finish and a clean work environment. Free consultation and color guidance available.",
                "₹ 15/sq ft onwards",
                "4.9 (120 reviews)",
                "- Interior & exterior painting\n- Wall preparation (putty, primer)\n- Texture painting\n- Wallpaper removal\n- Damage repair & touch-ups"
        ));

        // Carpenter Data
        homeMaintenanceServices.add(new Service(
                R.drawable.homeservice, // Assuming homeservice is your carpenter image
                "Carpenter",
                "Skilled carpentry for furniture repair, custom work, and installations.",
                "Expert carpenters providing a wide range of services including furniture repair, custom furniture design, modular kitchen installation, door/window fitting, and wooden partition work. We combine craftsmanship with modern tools to deliver durable and aesthetically pleasing results.",
                "₹ 300 onwards (per hour)",
                "4.6 (150 reviews)",
                "- Furniture repair & assembly\n- Custom furniture design\n- Door & window installation\n- Cabinetry & shelving\n- Wooden flooring repair"
        ));

        // AC Technician Data
        homeMaintenanceServices.add(new Service(
                R.drawable.homerepair, // Assuming you have an AC technician image
                "AC Technician",
                "Expert AC repair, service, and installation.",
                "Professional AC technicians for all brands and types of air conditioners. Services include routine servicing, gas refilling, repairs of common issues like cooling problems or leakage, and new AC installation. Extend the life of your AC with our reliable and timely service.",
                "₹ 600 onwards",
                "4.7 (200 reviews)",
                "- AC servicing & cleaning\n- Gas refilling\n- Compressor repair\n- Installation & uninstallation\n- Troubleshooting & diagnostics"
        ));

        // Tile Worker Data
        homeMaintenanceServices.add(new Service(
                R.drawable.homerepair, // Assuming you have a tile worker image
                "Tile Worker",
                "Precise tile installation and repair services.",
                "Skilled tile workers for professional flooring, wall tiling, and bathroom tiling. We handle ceramic, porcelain, mosaic, and natural stone tiles. Services include new installation, re-grouting, repair of broken tiles, and waterproofing solutions for bathrooms.",
                "₹ 20/sq ft onwards",
                "4.5 (90 reviews)",
                "- Floor & wall tiling\n- Bathroom tiling\n- Re-grouting\n- Tile repair & replacement\n- Waterproofing"
        ));

        CardAdapter homeMaintenanceAdapter = new CardAdapter(homeMaintenanceServices);
        homemaintain.setAdapter(homeMaintenanceAdapter);

        // --- Household Services ---
        RecyclerView householdServiceRecyclerView = findViewById(R.id.householdservice_scroll);
        // Corrected: Setting LinearLayoutManager for householdservice_scroll
        householdServiceRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Service> householdServices = new ArrayList<>();

        // House Cleaning Data
        householdServices.add(new Service(
                R.drawable.homeservice, // Assuming you have a house cleaning image
                "House Cleaning",
                "Professional deep cleaning services for sparkling homes.",
                "Comprehensive house cleaning services for residential properties. Includes deep cleaning of kitchens, bathrooms, living areas, and bedrooms. Our trained staff use eco-friendly products and advanced equipment to ensure a hygienic and spotless environment. Customizable cleaning packages available.",
                "₹ 1200 onwards",
                "4.9 (300 reviews)",
                "- Full home deep cleaning\n- Kitchen & bathroom sanitization\n- Floor scrubbing & polishing\n- Dusting & vacuuming\n- Window cleaning"
        ));

        // Beautician Data
        householdServices.add(new Service(
                R.drawable.homeservice, // Assuming you have a beautician image
                "Beautician",
                "At-home beauty services for your convenience.",
                "Experience salon-quality beauty services at the comfort of your home. Our certified beauticians offer a wide range of services including facials, waxing, manicure, pedicure, hair styling, and bridal makeup. Using branded products for best results.",
                "₹ 350 onwards",
                "4.7 (280 reviews)",
                "- Facials & skincare\n- Waxing & threading\n- Manicure & Pedicure\n- Hair styling & treatments\n- Bridal & party makeup"
        ));

        CardAdapter householdServiceAdapter = new CardAdapter(householdServices);
        householdServiceRecyclerView.setAdapter(householdServiceAdapter);

        // --- Lifestyle & Event Services ---
        RecyclerView lifestyleRecyclerView = findViewById(R.id.lifestyle_scroll);
        // Corrected: Setting LinearLayoutManager for lifestyle_scroll
        lifestyleRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Service> lifestyleServices = new ArrayList<>();

        // Tourist Guide Data
        lifestyleServices.add(new Service(
                R.drawable.homeservice, // Assuming you have a tourist guide image
                "Tourist Guide",
                "Explore the city with knowledgeable local guides.",
                "Discover the hidden gems and rich history of Bhubaneswar with our experienced tourist guides. We offer personalized tours for individuals and groups, covering historical sites, cultural landmarks, and local culinary experiences. Multi-lingual guides available upon request.",
                "₹ 1000 onwards (per day)",
                "4.9 (95 reviews)",
                "- City tours & sightseeing\n- Historical site explanations\n- Cultural immersion\n- Food tours\n- Custom itineraries"
        ));

        // Interior Designer Data
        lifestyleServices.add(new Service(
                R.drawable.homeservice, // Assuming you have an interior designer image
                "Interior Designer",
                "Create your dream space with expert interior design.",
                "Transform your home or office with our creative and functional interior design solutions. From conceptualization to execution, we handle space planning, furniture selection, lighting, color schemes, and material sourcing. Residential and commercial projects undertaken.",
                "Custom Quote",
                "4.8 (70 reviews)",
                "- Space planning\n- Furniture & decor selection\n- Color consultation\n- Lighting design\n- 3D rendering & visualization"
        ));

        CardAdapter lifestyleAdapter = new CardAdapter(lifestyleServices);
        lifestyleRecyclerView.setAdapter(lifestyleAdapter);

        // --- Bottom Navigation Button Listeners ---
        findViewById(R.id.cart_img).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.deshisnap.cart_page.CartPage.class));
        });

        findViewById(R.id.booking_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BookingsStatusDetails.class));
        });
        findViewById(R.id.inbox_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotificationPage.class));
        });

        findViewById(R.id.profile_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Profile_page.class));
        });
    }
}