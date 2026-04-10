package com.example.minipin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    private TextView detailTitle, detailDescription;
    private ImageView detailImage;
    private Button btnShare, btnCall;
    private PinDatabaseHelper dbHelper;

    private String currentTitle = "";
    private String currentDescription = "";
    private String currentImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = new PinDatabaseHelper(this);
        initializeViews();
        loadPinDetails();
        setupListeners();
    }

    private void initializeViews() {
        detailTitle = findViewById(R.id.detailTitle);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);
        btnShare = findViewById(R.id.btnShare);
        btnCall = findViewById(R.id.btnCall);
    }

    private void loadPinDetails() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String imagePath = intent.getStringExtra("imagePath");

        currentTitle = title != null ? title : "";
        currentDescription = description != null ? description : "";
        currentImagePath = imagePath != null ? imagePath : "";

        detailTitle.setText(currentTitle);
        detailDescription.setText(currentDescription);

        // Load image using Glide
        if (!currentImagePath.isEmpty()) {
            Glide.with(this)
                    .load(currentImagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(detailImage);
        } else {
            detailImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void setupListeners() {
        btnShare.setOnClickListener(v -> sharePin());
        btnCall.setOnClickListener(v -> savePin());
    }

    private void sharePin() {
        String title = detailTitle.getText().toString();
        String description = detailDescription.getText().toString();
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this pin: " + title + " - " + description);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void savePin() {
        boolean success = dbHelper.addPin(currentTitle, currentDescription, currentImagePath);
        if (success) {
            Toast.makeText(this, "Pin saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to save pin", Toast.LENGTH_SHORT).show();
        }
    }
}
