package com.example.cardiniamensshed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup Drawer Toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Ensure ActionBar is not null
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this::onBottomNavigationItemSelected);

        // Load the default fragment only if no fragment is currently loaded
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set default selection
        }

        // Load User Profile in Navigation Header
        loadNavigationHeader(navigationView);
    }

    private void loadNavigationHeader(NavigationView navigationView) {
        // Get the header view
        View headerView = navigationView.getHeaderView(0);

        // Find the profile image and username TextView in the header
        ImageView profileImageView = headerView.findViewById(R.id.nav_header_image);
        TextView usernameTextView = headerView.findViewById(R.id.nav_header_username);

        // Get the current user ID
        String userId = auth.getCurrentUser().getUid();

        // Fetch user details from Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get username and profile image URL
                        String username = documentSnapshot.getString("username");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        // Set the username in the header
                        usernameTextView.setText(username);

                        // Load the profile image using Glide
                        Glide.with(this)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.profile_image_background) // Default placeholder
                                .error(R.drawable.profile_image_error) // Fallback on error
                                .into(profileImageView);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast or log the error)
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new DashboardFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Sync Bottom Navigation
        } else if (itemId == R.id.nav_join_event) {
            selectedFragment = new JoinEventFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_join_event); // Sync Bottom Navigation
        } else if (itemId == R.id.nav_book_event) {
            selectedFragment = new BookEventFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_book_event); // Sync Bottom Navigation
        } else if (itemId == R.id.nav_chat_room) {
            selectedFragment = new ChatRoomFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_chat_room); // Sync Bottom Navigation
        } else if (itemId == R.id.nav_attendance) {
            selectedFragment = new AttendanceFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set "Home" in Bottom Navigation
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Sync Bottom Navigation
        } else if (itemId == R.id.nav_help) {
            // Load the WebViewFragment for Help & Support
            selectedFragment = WebViewFragment.newInstance("https://cardiniamensshed.org.au/");
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            drawerLayout.closeDrawer(GravityCompat.START, false);
            navigateToLogin();
            return true;
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean onBottomNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new DashboardFragment();
        } else if (itemId == R.id.nav_join_event) {
            selectedFragment = new JoinEventFragment();
        } else if (itemId == R.id.nav_book_event) {
            selectedFragment = new BookEventFragment();
        } else if (itemId == R.id.nav_chat_room) {
            selectedFragment = new ChatRoomFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle drawer toggle clicks
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Reload the JoinEventFragment to refresh its data.
     */
    // In DashboardActivity.java:
    public void reloadJoinEventFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("JoinEventFragment");
        if (fragment != null && fragment instanceof JoinEventFragment) {
            ((JoinEventFragment) fragment).refreshEvents();
        }
    }
}