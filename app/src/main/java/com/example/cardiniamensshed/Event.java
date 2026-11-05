package com.example.cardiniamensshed;

public class Event {

    private String id;           // Unique ID of the event
    private String name;         // Name of the event
    private String owner;        // Owner of the event (user ID or name)
    private String description;  // Description of the event
    private String category;     // Category of the event
    private long timestamp;      // Timestamp of the event
    private String imageUrl;     // URL of the event image
    private double price;        // Price of the product associated with the event
    private int quantity;        // Quantity of the product associated with the event
    private String type;         // Type of the event (e.g., Workshop, Seminar)

    // Default constructor (required for Firestore)
    public Event() {
    }

    // Parameterized constructor
    public Event(String id, String name, String owner, String description, String category, long timestamp, String imageUrl, double price, int quantity, String type) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.category = category;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}