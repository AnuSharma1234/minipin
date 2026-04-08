package com.example.minipin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout pinsContainer;

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

        pinsContainer = findViewById(R.id.pinsContainer);
        Button btnAddPin = findViewById(R.id.btnAddPin);
        Button btnViewPins = findViewById(R.id.btnViewPins);

        // Check if first time user
        SharedPreferences sharedPreferences = getSharedPreferences("MiniPinPrefs", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            // Launch Welcome Activity
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }

        // Add static pins
        loadStaticPins();

        // Button listeners
        btnAddPin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPinActivity.class);
            startActivity(intent);
        });

        btnViewPins.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewPinsActivity.class);
            startActivity(intent);
        });
    }

    private void loadStaticPins() {
        // Static pin data
        String[][] staticPins = {
                {"Beautiful Sunset", "A stunning sunset over the mountains"},
                {"Ocean Waves", "Waves crashing on the sandy beach"},
                {"Forest Trail", "A peaceful walk through the forest"},
                {"City Lights", "The city skyline at night"}
        };

        int[] drawableIds = {
                android.R.drawable.ic_menu_help,
                android.R.drawable.ic_menu_help,
                android.R.drawable.ic_menu_help,
                android.R.drawable.ic_menu_help
        };

        for (int i = 0; i < staticPins.length; i++) {
            addPinView(staticPins[i][0], staticPins[i][1], drawableIds[i]);
        }
    }

    private void addPinView(String title, String description, int drawableId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pinView = inflater.inflate(R.layout.pin_item, pinsContainer, false);

        ImageView imageView = pinView.findViewById(R.id.pinImage);
        TextView titleView = pinView.findViewById(R.id.pinTitle);
        TextView descView = pinView.findViewById(R.id.pinDescription);

        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        imageView.setImageDrawable(drawable);
        titleView.setText(title);
        descView.setText(description);

        pinView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            startActivity(intent);
        });

        pinsContainer.addView(pinView);
    }
}
