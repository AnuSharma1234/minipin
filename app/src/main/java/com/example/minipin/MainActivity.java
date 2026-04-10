package com.example.minipin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPins;
    private PinAdapter pinAdapter;
    private FrameLayout emptyState;
    private BottomNavigationView bottomNavigationView;
    private List<Pin> pins;

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

        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();

        // Check if first time user
        SharedPreferences sharedPreferences = getSharedPreferences("MiniPinPrefs", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }

        loadStaticPins();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning from other activities
        loadStaticPins();
    }

    private void initializeViews() {
        recyclerViewPins = findViewById(R.id.recyclerViewPins);
        emptyState = findViewById(R.id.emptyState);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        pins = new ArrayList<>();
        pinAdapter = new PinAdapter(pins, this);
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerViewPins.setLayoutManager(gridLayoutManager);
        recyclerViewPins.setAdapter(pinAdapter);

        pinAdapter.setOnPinClickListener(pin -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("title", pin.getTitle());
            intent.putExtra("description", pin.getDescription());
            intent.putExtra("imagePath", pin.getImagePath());
            startActivity(intent);
        });

        pinAdapter.setOnPinLongClickListener((pin, position) -> {
            // Can implement long-press actions if needed
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home, refresh
                loadStaticPins();
                return true;
            } else if (itemId == R.id.nav_saved) {
                Intent intent = new Intent(MainActivity.this, ViewPinsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_create) {
                Intent intent = new Intent(MainActivity.this, AddPinActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        // Set home as default
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadStaticPins() {
        pins.clear();
        
        // Static pin data with sample images
        String[][] staticPins = {
                {"Funny Cat", "cat_yeah mood unlocked"},
                {"Manga Climber", "Peak panel energy"},
                {"Ghibli Mountains", "Soft sky and calm vibes"},
                {"Osaka Chaos", "Certified funny Osaka moment"},
                {"We Bears", "Another funny classic"}
        };

        int[] drawableIds = {
                R.drawable.cat_yeah,
                R.drawable.climber_pfp,
                R.drawable.mountains_ghibli,
                R.drawable.osaka_in_iraq,
                R.drawable.we_bears
        };

        for (int i = 0; i < staticPins.length; i++) {
            Pin pin = new Pin();
            pin.setTitle(staticPins[i][0]);
            pin.setDescription(staticPins[i][1]);
            // have to get the packagename ( from a user's ssytem and then add the URI base of static drawable Ids )
            String imagePath = "android.resource://" + getPackageName() + "/" + drawableIds[i];
            pin.setImagePath(imagePath);
            pins.add(pin);
        }

        pinAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (pins.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewPins.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerViewPins.setVisibility(View.VISIBLE);
        }
    }
}

