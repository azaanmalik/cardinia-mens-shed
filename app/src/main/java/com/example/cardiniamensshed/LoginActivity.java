package com.example.cardiniamensshed;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button loginBtn;
    ImageView togglePassword;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginButton);
        togglePassword = findViewById(R.id.toggleLoginPassword);
        progressBar = findViewById(R.id.loginProgressBar);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        TextView signupPrompt = findViewById(R.id.signupPrompt);

        // Show/Hide password
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePassword.setImageResource(R.drawable.ic_eye_off);
            } else {
                password.setTransformationMethod(null);
                togglePassword.setImageResource(R.drawable.ic_eye);
            }
            isPasswordVisible = !isPasswordVisible;
            password.setSelection(password.getText().length());
        });

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                email.setError("Enter a valid email address");
                return;
            }

            if (userPass.length() < 6) {
                password.setError("Password must be at least 6 characters");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

        signupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }
}
