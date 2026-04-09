package com.example.minipin;

public class Pin {
    private int id;
    private String title;
    private String description;
    private String imagePath;

    // Constructor with all fields
    public Pin(int id, String title, String description, String imagePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }

    // Constructor without image path (for backward compatibility)
    public Pin(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = "";
    }

    // Default constructor
    public Pin() {
        this.imagePath = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath != null ? imagePath : "";
    }
}

