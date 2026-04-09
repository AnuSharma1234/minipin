package com.example.minipin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class PinDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "minipin.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_PINS = "pins";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_PATH = "image_path";

    public PinDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_PINS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                COLUMN_IMAGE_PATH + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add image_path column if upgrading from v1
            db.execSQL("ALTER TABLE " + TABLE_PINS + " ADD COLUMN " + COLUMN_IMAGE_PATH + " TEXT");
        }
    }

    public boolean addPin(String title, String description, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_IMAGE_PATH, imagePath != null ? imagePath : "");
        long result = db.insert(TABLE_PINS, null, values);
        db.close();
        return result != -1;
    }

    // Backward compatible version without image path
    public boolean addPin(String title, String description) {
        return addPin(title, description, "");
    }

    public List<Pin> getAllPins() {
        List<Pin> pins = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PINS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String imagePath = "";
                
                // Check if column exists before trying to read it
                int imagePathIndex = cursor.getColumnIndex(COLUMN_IMAGE_PATH);
                if (imagePathIndex != -1) {
                    imagePath = cursor.getString(imagePathIndex);
                }
                
                pins.add(new Pin(id, title, description, imagePath != null ? imagePath : ""));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return pins;
    }

    public boolean deletePin(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PINS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Pin getPinById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PINS, null, COLUMN_ID + " = ?", 
                new String[]{String.valueOf(id)}, null, null, null);

        Pin pin = null;
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            String imagePath = "";
            
            int imagePathIndex = cursor.getColumnIndex(COLUMN_IMAGE_PATH);
            if (imagePathIndex != -1) {
                imagePath = cursor.getString(imagePathIndex);
            }
            
            pin = new Pin(id, title, description, imagePath != null ? imagePath : "");
        }
        cursor.close();
        db.close();
        return pin;
    }
}

