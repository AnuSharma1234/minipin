package com.example.minipin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ViewPinsActivity extends AppCompatActivity {

    private LinearLayout savedPinsContainer;
    private Button btnBackFromView;
    private TextView noDataText;
    private PinDatabaseHelper dbHelper;
    private List<Pin> pins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pins);

        savedPinsContainer = findViewById(R.id.savedPinsContainer);
        btnBackFromView = findViewById(R.id.btnBackFromView);
        noDataText = findViewById(R.id.noDataText);

        dbHelper = new PinDatabaseHelper(this);

        loadSavedPins();

        btnBackFromView.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload pins when returning from other activities
        loadSavedPins();
    }

    private void loadSavedPins() {
        savedPinsContainer.removeAllViews();
        pins = dbHelper.getAllPins();

        if (pins.isEmpty()) {
            noDataText.setVisibility(View.VISIBLE);
        } else {
            noDataText.setVisibility(View.GONE);
            for (Pin pin : pins) {
                addSavedPinView(pin);
            }
        }
    }

    private void addSavedPinView(Pin pin) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pinView = inflater.inflate(R.layout.saved_pin_item, savedPinsContainer, false);

        TextView titleView = pinView.findViewById(R.id.savedPinTitle);
        TextView descView = pinView.findViewById(R.id.savedPinDescription);

        titleView.setText(pin.getTitle());
        descView.setText(pin.getDescription());

        // Click to view detail
        pinView.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPinsActivity.this, DetailActivity.class);
            intent.putExtra("title", pin.getTitle());
            intent.putExtra("description", pin.getDescription());
            startActivity(intent);
        });

        // Long press to delete
        pinView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(pin.getId(), pin.getTitle());
            return true;
        });

        savedPinsContainer.addView(pinView);
    }

    private void showDeleteConfirmationDialog(int pinId, String pinTitle) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pin")
                .setMessage("Delete \"" + pinTitle + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = dbHelper.deletePin(pinId);
                    if (success) {
                        loadSavedPins();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
