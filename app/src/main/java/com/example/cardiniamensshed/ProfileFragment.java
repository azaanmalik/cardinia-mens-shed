package com.example.cardiniamensshed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private ImageView addImageIcon;
    private EditText usernameEditText, emailEditText;
    private ProgressBar progressBar;
    private Uri newProfileImageUri;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImageView);
        addImageIcon = view.findViewById(R.id.addImageIcon);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        progressBar = view.findViewById(R.id.profileProgressBar);
        Button updateButton = view.findViewById(R.id.updateButton);
        TextView logoutTextView = view.findViewById(R.id.logoutTextView);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Protect against unauthenticated access
        if (currentUser == null) {
            navigateToLogin();
            return view;
        }

        loadUserProfile();

        // Action Listeners
        updateButton.setOnClickListener(v -> updateUserProfile());
        logoutTextView.setOnClickListener(v -> logout());
        profileImage.setOnClickListener(v -> changeProfileImage());
        addImageIcon.setOnClickListener(v -> changeProfileImage());

        return view;
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Ensure fragment is still attached before updating UI
                    if (isAdded()) {
                        if (documentSnapshot.exists()) {
                            usernameEditText.setText(documentSnapshot.getString("username"));
                            emailEditText.setText(documentSnapshot.getString("email"));

                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.profile_image_background)
                                        .error(R.drawable.profile_image_error)
                                        .into(profileImage);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changeProfileImage() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(512, 512)
                .createIntent(intent -> {
                    startActivityForResult(intent, 101);
                    return null;
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK && data != null) {
            newProfileImageUri = data.getData();
            if (isAdded()) {
                Glide.with(requireContext())
                        .load(newProfileImageUri)
                        .circleCrop()
                        .into(profileImage);
                addImageIcon.setVisibility(View.GONE);
            }
        }
    }

    private void updateUserProfile() {
        String updatedUsername = usernameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();

        if (updatedUsername.isEmpty() || updatedEmail.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (newProfileImageUri != null) {
            SupabaseUploader.uploadProfilePicture(requireContext(), newProfileImageUri, currentUser, new SupabaseUploader.UploadCallback() {
                @Override
                public void onUploadComplete(String url) {
                    if (isAdded()) {
                        updateFirestoreProfile(updatedUsername, updatedEmail, url);
                    }
                }

                @Override
                public void onUploadFailed(String error) {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ProfileUpdate", "Image upload failed: " + error);
                        Toast.makeText(requireContext(), "Error uploading image: " + error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            updateFirestoreProfile(updatedUsername, updatedEmail, null);
        }
    }

    private void updateFirestoreProfile(String username, String email, @Nullable String newImageUrl) {
        if (!isAdded()) return; // Guard against detached fragment

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("email", email);

        if (newImageUrl != null) {
            updates.put("profileImageUrl", newImageUrl);
        }

        firestore.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ProfileUpdate", "Firestore update failed: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error updating profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}