package com.example.minipin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ViewPinsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSavedPins;
    private FrameLayout emptyStateSaved;
    private PinAdapter pinAdapter;
    private PinDatabaseHelper dbHelper;
    private List<Pin> pins;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pins);

        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        
        dbHelper = new PinDatabaseHelper(this);
        loadSavedPins();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedPins();
    }

    private void initializeViews() {
        recyclerViewSavedPins = findViewById(R.id.recyclerViewSavedPins);
        emptyStateSaved = findViewById(R.id.emptyStateSaved);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        pinAdapter = new PinAdapter(pins, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerViewSavedPins.setLayoutManager(gridLayoutManager);
        recyclerViewSavedPins.setAdapter(pinAdapter);

        pinAdapter.setOnPinClickListener(pin -> {
            Intent intent = new Intent(ViewPinsActivity.this, DetailActivity.class);
            intent.putExtra("title", pin.getTitle());
            intent.putExtra("description", pin.getDescription());
            intent.putExtra("imagePath", pin.getImagePath());
            startActivity(intent);
        });

        pinAdapter.setOnPinLongClickListener((pin, position) -> {
            showDeleteConfirmationDialog(pin.getId(), pin.getTitle(), position);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(ViewPinsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_saved) {
                // Already on saved
                return true;
            } else if (itemId == R.id.nav_create) {
                Intent intent = new Intent(ViewPinsActivity.this, AddPinActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        // Set saved as selected
        bottomNavigationView.setSelectedItemId(R.id.nav_saved);
    }

    private void loadSavedPins() {
        pins = dbHelper.getAllPins();
        
        if (pinAdapter != null) {
            pinAdapter.updateList(pins);
        } else {
            pinAdapter = new PinAdapter(pins, this);
            recyclerViewSavedPins.setAdapter(pinAdapter);
        }
        
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (pins.isEmpty()) {
            emptyStateSaved.setVisibility(View.VISIBLE);
            recyclerViewSavedPins.setVisibility(View.GONE);
        } else {
            emptyStateSaved.setVisibility(View.GONE);
            recyclerViewSavedPins.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(int pinId, String pinTitle, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pin")
                .setMessage("Delete \"" + pinTitle + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = dbHelper.deletePin(pinId);
                    if (success) {
                        pinAdapter.removeItem(position);
                        pins.remove(position);
                        updateEmptyState();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

