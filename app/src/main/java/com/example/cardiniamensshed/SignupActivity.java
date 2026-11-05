package com.example.cardiniamensshed;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText username, email, password, confirmPassword;
    Button signupBtn;
    ImageView togglePass, toggleConfirm, profileImageView;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    Uri profileImageUri;
    boolean isPassVisible = false, isConfirmVisible = false;

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signupBtn = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.SignupProgressBar);
        togglePass = findViewById(R.id.togglePassword);
        toggleConfirm = findViewById(R.id.toggleConfirmPassword);
        profileImageView = findViewById(R.id.profileImage);
        TextView loginPrompt = findViewById(R.id.loginPrompt);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        profileImageUri = result.getData().getData();
                        Glide.with(this).load(profileImageUri).circleCrop().into(profileImageView);
                    }
                }
        );

        profileImageView.setOnClickListener(v -> openImagePicker());
        togglePass.setOnClickListener(v -> togglePasswordVisibility(password, togglePass));
        toggleConfirm.setOnClickListener(v -> togglePasswordVisibility(confirmPassword, toggleConfirm));
        signupBtn.setOnClickListener(v -> handleSignup());
        loginPrompt.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void createUserWithProfileImage(String userName, String userEmail, String userPass) {
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        SupabaseUploader.uploadProfilePicture(this, profileImageUri, user, new SupabaseUploader.UploadCallback() {
                            @Override
                            public void onUploadComplete(String imageUrl) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("uid", user.getUid());
                                userMap.put("username", userName);
                                userMap.put("email", userEmail);
                                userMap.put("profileImageUrl", imageUrl); // Save the URL to Firestore
                                userMap.put("createdAt", System.currentTimeMillis());

                                firestore.collection("users").document(user.getUid())
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            showError("Failed to save user data: " + e.getMessage());
                                        });
                            }

                            @Override
                            public void onUploadFailed(String error) {
                                progressBar.setVisibility(View.GONE);
                                showError("Image upload failed: " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Signup failed: " + e.getMessage());
                });
    }

    private void togglePasswordVisibility(EditText field, ImageView toggle) {
        boolean visible = field.getTransformationMethod() == null;
        if (visible) {
            field.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggle.setImageResource(R.drawable.ic_eye_off);
        } else {
            field.setTransformationMethod(null);
            toggle.setImageResource(R.drawable.ic_eye);
        }
        field.setSelection(field.getText().length());
    }

    private void openImagePicker() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(512, 512)
                .createIntent(intent -> {
                    imagePickerLauncher.launch(intent);
                    return null;
                });
    }

    private void handleSignup() {
        String userName = username.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();
        String userConfirm = confirmPassword.getText().toString().trim();

        if (userName.isEmpty() || userEmail.isEmpty() || userPass.isEmpty() || userConfirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Enter a valid email");
            return;
        }

        if (userPass.length() < 6) {
            password.setError("Minimum 6 characters required");
            return;
        }

        if (!userPass.equals(userConfirm)) {
            confirmPassword.setError("Passwords don't match");
            return;
        }

        if (profileImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        createUserWithProfileImage(userName, userEmail, userPass);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Signup Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
