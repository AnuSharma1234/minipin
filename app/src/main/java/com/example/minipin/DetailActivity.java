package com.example.minipin;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class DetailActivity extends AppCompatActivity {

    private TextView detailTitle, detailDescription;
    private ImageView detailImage;
    private Button btnShare, btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailTitle = findViewById(R.id.detailTitle);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);
        btnShare = findViewById(R.id.btnShare);
        btnCall = findViewById(R.id.btnCall);

        // Get data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        detailTitle.setText(title);
        detailDescription.setText(description);

        // Set a placeholder image
        Drawable drawable = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_help);
        detailImage.setImageDrawable(drawable);

        // Share button
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this pin: " + title + " - " + description);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        // Call button (dummy)
        btnCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:5551234567"));
            try {
                startActivity(callIntent);
            } catch (Exception e) {
                // Handle exception if dialer is not available
            }
        });
    }
}
