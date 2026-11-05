package com.example.cardiniamensshed;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText resetEmail;
    Button sendResetLink;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mAuth = FirebaseAuth.getInstance();
        resetEmail = findViewById(R.id.resetEmail);
        sendResetLink = findViewById(R.id.sendResetLink);

        sendResetLink.setOnClickListener(v -> {
            String email = resetEmail.getText().toString().trim();

            // Validate email input
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send password reset link via Firebase Auth
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show();
                            finish();  // Optionally, redirect to login screen
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "An error occurred";
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}


