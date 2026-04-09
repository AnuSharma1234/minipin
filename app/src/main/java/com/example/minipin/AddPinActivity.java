package com.example.minipin;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddPinActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription;
    private Button btnSubmit, btnBack, btnChooseImage;
    private ImageView selectedImage;
    private TextView characterCount;
    private BottomNavigationView bottomNavigationView;
    private PinDatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private static final String CHANNEL_ID = "pin_notifications";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pin);

        initializeViews();
        setupImagePicker();
        setupBottomNavigation();
        
        dbHelper = new PinDatabaseHelper(this);

        // Create notification channel
        createNotificationChannel();

        // Check and request notification permission for Android 13+
        checkNotificationPermission();

        // Text watcher to validate inputs
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
                updateCharacterCount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);

        // Image picker button
        btnChooseImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(AddPinActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddPinActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                openImagePicker();
            }
        });

        // Submit button
        btnSubmit.setOnClickListener(v -> savePin());

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        selectedImage = findViewById(R.id.selectedImage);
        characterCount = findViewById(R.id.characterCount);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            selectedImage.setImageURI(imageUri);
                        }
                    }
                });
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(AddPinActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_saved) {
                    Intent intent = new Intent(AddPinActivity.this, ViewPinsActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_create) {
                    // Already on create
                    return true;
                }
                return false;
            });
            
            // Set create as selected
            bottomNavigationView.setSelectedItemId(R.id.nav_create);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void updateCharacterCount() {
        int count = etDescription.getText().toString().length();
        characterCount.setText(count + "/500");
    }

    private void savePin() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (!title.isEmpty() && !description.isEmpty()) {
            String imagePath = "";
            
            // Save image if selected
            if (selectedImageUri != null) {
                imagePath = saveImageToInternalStorage(selectedImageUri);
            }

            boolean success = dbHelper.addPin(title, description, imagePath);
            if (success) {
                Toast.makeText(AddPinActivity.this, "Pin saved successfully!", Toast.LENGTH_SHORT).show();
                showNotification("New Pin Added", title);
                finish();
            } else {
                Toast.makeText(AddPinActivity.this, "Error saving pin", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File directory = getFilesDir();
            File imageFile = new File(directory, "pin_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void validateForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        btnSubmit.setEnabled(!title.isEmpty() && !description.isEmpty() && description.length() <= 500);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pin Notifications";
            String description = "Notifications for new pins";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String pinTitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(pinTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}


    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUri = imageUri;
                            selectedImage.setImageURI(imageUri);
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void updateCharacterCount() {
        int count = etDescription.getText().toString().length();
        characterCount.setText(count + "/500");
    }

    private void savePin() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (!title.isEmpty() && !description.isEmpty()) {
            String imagePath = "";
            
            // Save image if selected
            if (selectedImageUri != null) {
                imagePath = saveImageToInternalStorage(selectedImageUri);
            }

            boolean success = dbHelper.addPin(title, description, imagePath);
            if (success) {
                Toast.makeText(AddPinActivity.this, "Pin saved successfully!", Toast.LENGTH_SHORT).show();
                showNotification("New Pin Added", title);
                finish();
            } else {
                Toast.makeText(AddPinActivity.this, "Error saving pin", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File directory = getFilesDir();
            File imageFile = new File(directory, "pin_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void validateForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        btnSubmit.setEnabled(!title.isEmpty() && !description.isEmpty() && description.length() <= 500);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pin Notifications";
            String description = "Notifications for new pins";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String pinTitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(pinTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
}


