package com.example.cohort;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

public class VerifyOTPActivity extends AppCompatActivity {
    private TextView phoneNumberText;
    private EditText verificationCodeInput;
    private Button verifyOTPButton;

    private ProgressBar progressBar;

    String verificationId;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("Profile images");

        phoneNumberText = findViewById(R.id.phoneNumberText);
        verificationCodeInput = findViewById(R.id.editTextVerificationCode);
        verifyOTPButton = findViewById(R.id.verifyOTPButton);
        progressBar = findViewById(R.id.progressBar);

        HashMap<String, String> map = (HashMap<String, String>) getIntent().getExtras().get("userMap");
        String phoneNumber = getIntent().getStringExtra("phone");
        Uri imageUri = (Uri) getIntent().getExtras().get("imageUri");

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
                            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

                            UploadTask uploadTask = fileReference.putFile(imageUri);
                            uploadTask.continueWithTask(task1 -> {
                                if (!task1.isSuccessful()) {
                                    throw Objects.requireNonNull(task1.getException());
                                }
                                return fileReference.getDownloadUrl();
                            }).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Uri downloadUri = task2.getResult();
                                    if (!(downloadUri == null)) {
                                        String imagePath = downloadUri.toString();
                                        com.example.cohort.User user = new com.example.cohort.User(firebaseAuth.getUid(), map.get("firstName"), map.get("lastName"), map.get("email"), phoneNumber, imagePath);
                                        databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(user).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                verifyOTPButton.setVisibility(View.VISIBLE);
                                                Toast.makeText(VerifyOTPActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(VerifyOTPActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                VerifyOTPActivity.this.startActivity(intent);
                                            } else {
                                                Toast.makeText(VerifyOTPActivity.this, Objects.requireNonNull(task3.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                verifyOTPButton.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                }
                            });
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

    private String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
}