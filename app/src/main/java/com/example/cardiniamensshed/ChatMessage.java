package com.example.cardiniamensshed;

public class ChatMessage {
    private String sender;
    private String senderName;
    private String profileImage;
    private String message;
    private long timestamp;
    private boolean isOwner;
    private double productPrice;
    private int productQuantity;
    private String productName;
    private boolean isPurchaseMessage;

    public ChatMessage() {}

    public ChatMessage(String sender, String senderName, String profileImage, String message, long timestamp,
                       boolean isOwner, double productPrice, int productQuantity,
                       String productName, boolean isPurchaseMessage) {
        this.sender = sender;
        this.senderName = senderName;
        this.profileImage = profileImage;
        this.message = message;
        this.timestamp = timestamp;
        this.isOwner = isOwner;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productName = productName;
        this.isPurchaseMessage = isPurchaseMessage;
    }

    public String getSender() { return sender; }
    public String getSenderName() { return senderName; }
    public String getProfileImage() { return profileImage; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isOwner() { return isOwner; }
    public double getProductPrice() { return productPrice; }
    public int getProductQuantity() { return productQuantity; }
    public String getProductName() { return productName; }
    public boolean isPurchaseMessage() { return isPurchaseMessage; }

    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}