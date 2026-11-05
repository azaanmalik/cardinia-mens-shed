package com.example.cardiniamensshed;

public class Participant {

    private String userId;

    // Default constructor (required for Firestore)
    public Participant() {
    }

    // Parameterized constructor
    public Participant(String userId) {
        this.userId = userId;
    }

    // Getter and Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}