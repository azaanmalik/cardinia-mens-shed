package com.example.cardiniamensshed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SplashActivity extends AppCompatActivity {
    // UI Elements
    private CardView logoContainer;
    private View bgCircle1, bgCircle2;
    private TextView appNameTextView, taglineTextView, versionTextView;
    private LinearLayout featureIconsContainer;
    private ProgressBar loadingProgressBar;

    // Animation durations
    private final int LOGO_ANIM_DURATION = 800;
    private final int TEXT_ANIM_DURATION = 500;
    private final int LOADING_ANIM_DURATION = 2000;
    private final int SPLASH_DISPLAY_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize UI elements
        initializeViews();

        // Start animations
        startAnimations();

        // Simulate loading process and navigate to main activity
        simulateLoading();
    }

    private void initializeViews() {
        logoContainer = findViewById(R.id.logoContainer);
        bgCircle1 = findViewById(R.id.bgCircle1);
        bgCircle2 = findViewById(R.id.bgCircle2);
        appNameTextView = findViewById(R.id.appNameTextView);
        taglineTextView = findViewById(R.id.taglineTextView);
        versionTextView = findViewById(R.id.versionTextView);
        featureIconsContainer = findViewById(R.id.featureIconsContainer);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }

    private void startAnimations() {
        // Create all animations
        AnimatorSet animatorSet = new AnimatorSet();

        // Background circles animation
        animateBackgroundCircles();

        // Logo animation - Scale + Fade in
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0.0f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0.0f, 1.0f);
        ObjectAnimator logoFadeIn = ObjectAnimator.ofFloat(logoContainer, "alpha", 0.0f, 1.0f);

        // App name animation - Fade in + Slide down
        ObjectAnimator nameSlideDown = ObjectAnimator.ofFloat(appNameTextView, "translationY", -20f, 0f);
        ObjectAnimator nameFadeIn = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0.0f, 1.0f);

        // Tagline animation - Fade in + Slide down
        ObjectAnimator taglineSlideDown = ObjectAnimator.ofFloat(taglineTextView, "translationY", -15f, 0f);
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(taglineTextView, "alpha", 0.0f, 1.0f);

        // Feature icons animation - Fade in + Slide up
        ObjectAnimator featureIconsSlideUp = ObjectAnimator.ofFloat(featureIconsContainer, "translationY", 20f, 0f);
        ObjectAnimator featureIconsFadeIn = ObjectAnimator.ofFloat(featureIconsContainer, "alpha", 0.0f, 1.0f);

        // Version text animation - Fade in
        ObjectAnimator versionFadeIn = ObjectAnimator.ofFloat(versionTextView, "alpha", 0.0f, 1.0f);

        // Loading bar animation - Fade in
        ObjectAnimator loadingBarFadeIn = ObjectAnimator.ofFloat(loadingProgressBar, "alpha", 0.0f, 1.0f);

        // Set animation properties
        logoScaleX.setInterpolator(new OvershootInterpolator());
        logoScaleY.setInterpolator(new OvershootInterpolator());
        logoFadeIn.setInterpolator(new DecelerateInterpolator());

        nameSlideDown.setInterpolator(new DecelerateInterpolator());
        taglineSlideDown.setInterpolator(new DecelerateInterpolator());
        featureIconsSlideUp.setInterpolator(new DecelerateInterpolator());

        // Build animation sequence
        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(logoScaleX, logoScaleY, logoFadeIn);
        logoAnimSet.setDuration(LOGO_ANIM_DURATION);

        AnimatorSet textAnimSet = new AnimatorSet();
        textAnimSet.playTogether(nameSlideDown, nameFadeIn);
        textAnimSet.setDuration(TEXT_ANIM_DURATION);

        AnimatorSet taglineAnimSet = new AnimatorSet();
        taglineAnimSet.playTogether(taglineSlideDown, taglineFadeIn);
        taglineAnimSet.setDuration(TEXT_ANIM_DURATION);

        AnimatorSet featureIconsAnimSet = new AnimatorSet();
        featureIconsAnimSet.playTogether(featureIconsSlideUp, featureIconsFadeIn);
        featureIconsAnimSet.setDuration(TEXT_ANIM_DURATION);

        // Combine all animations with sequence
        animatorSet.play(logoAnimSet).before(textAnimSet);
        animatorSet.play(textAnimSet).before(taglineAnimSet);
        animatorSet.play(taglineAnimSet).before(featureIconsAnimSet);
        animatorSet.play(featureIconsAnimSet).with(loadingBarFadeIn);
        animatorSet.play(featureIconsAnimSet).with(versionFadeIn);

        // Start the animation sequence
        animatorSet.start();
    }

    private void animateBackgroundCircles() {
        // Animate first background circle
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(bgCircle1, "scaleX", 0.95f, 1.05f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(bgCircle1, "scaleY", 0.95f, 1.05f);
        scaleX1.setRepeatMode(ValueAnimator.REVERSE);
        scaleY1.setRepeatMode(ValueAnimator.REVERSE);
        scaleX1.setRepeatCount(ValueAnimator.INFINITE);
        scaleY1.setRepeatCount(ValueAnimator.INFINITE);
        scaleX1.setDuration(3000);
        scaleY1.setDuration(3000);
        scaleX1.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY1.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate second background circle (slightly faster and opposite phase)
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(bgCircle2, "scaleX", 1.05f, 0.95f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(bgCircle2, "scaleY", 1.05f, 0.95f);
        scaleX2.setRepeatMode(ValueAnimator.REVERSE);
        scaleY2.setRepeatMode(ValueAnimator.REVERSE);
        scaleX2.setRepeatCount(ValueAnimator.INFINITE);
        scaleY2.setRepeatCount(ValueAnimator.INFINITE);
        scaleX2.setDuration(2500);
        scaleY2.setDuration(2500);
        scaleX2.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY2.setInterpolator(new AccelerateDecelerateInterpolator());

        // Start animations
        scaleX1.start();
        scaleY1.start();
        scaleX2.start();
        scaleY2.start();
    }

    private void simulateLoading() {
        // Animate progress bar from 0 to 100%
        final ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(LOADING_ANIM_DURATION);
        progressAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            loadingProgressBar.setProgress(animatedValue);
        });

        // Slight delay before starting progress animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progressAnimator.start();
        }, LOGO_ANIM_DURATION);

        // Navigate to main activity when loading completes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Create fun exit animation
            startExitAnimation();
        }, SPLASH_DISPLAY_TIME);
    }

    private void startExitAnimation() {
        // Create a scale up and fade out animation for the entire layout
        View rootView = findViewById(android.R.id.content).getRootView();

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(rootView, "scaleX", 1.0f, 1.2f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(rootView, "scaleY", 1.0f, 1.2f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rootView, "alpha", 1.0f, 0.0f);

        AnimatorSet exitAnimation = new AnimatorSet();
        exitAnimation.playTogether(scaleUpX, scaleUpY, fadeOut);
        exitAnimation.setDuration(500);
        exitAnimation.setInterpolator(new AccelerateDecelerateInterpolator());

        exitAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Navigate to main activity
                startMainActivity();
            }
        });

        exitAnimation.start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);

        // Add custom transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Close splash activity
        finish();
    }
}