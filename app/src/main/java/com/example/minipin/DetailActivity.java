package com.example.minipin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    private TextView detailTitle, detailDescription;
    private ImageView detailImage;
    private Button btnShare, btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

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

        detailTitle.setText(title != null ? title : "");
        detailDescription.setText(description != null ? description : "");

        // Load image using Glide
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
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
        btnCall.setOnClickListener(v -> makeCall());
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

    private void makeCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:5551234567"));
        try {
            startActivity(callIntent);
        } catch (Exception e) {
            // Handle exception if dialer is not available
        }
    }
}

