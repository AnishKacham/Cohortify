package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;

public class LoginVerifyOTPActivity extends AppCompatActivity {
    private TextView phoneNumberText;
    private EditText verificationCodeInput;
    private Button verifyOTPButton;

    String verificationId;

    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_verify_otp);

        firebaseAuth = FirebaseAuth.getInstance();

        phoneNumberText = findViewById(R.id.phoneNumberText);
        verificationCodeInput = findViewById(R.id.editTextVerificationCode);
        verifyOTPButton = findViewById(R.id.verifyOTPButton);
        progressBar = findViewById(R.id.progressBar);

        String phoneNumber = getIntent().getStringExtra("phone");

        phoneNumberText.setText(phoneNumberText.getText().toString() + " " + phoneNumber);

        verificationId = getIntent().getStringExtra("verificationId");

        verifyOTPButton.setOnClickListener(view -> {
            String verificationCode = verificationCodeInput.getText().toString().trim();

            progressBar.setVisibility(View.VISIBLE);
            verifyOTPButton.setVisibility(View.GONE);

            if (verificationCode.isEmpty()) {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                verifyOTPButton.setVisibility(View.VISIBLE);
            } else {
                if (verificationId != null) {
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId, verificationCode
                    );
                    firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            verifyOTPButton.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginVerifyOTPActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            verifyOTPButton.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Toast.makeText(this, "Please check internet connection", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    verifyOTPButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}