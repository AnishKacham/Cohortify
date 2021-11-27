package com.example.cohort;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {
    private EditText phoneInput;
    private Button getOTPButton;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthOptions options;

    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private ProgressBar progressBar;
    private DatabaseReference usersReference;

    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        firebaseAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        HashMap<String, String> map = (HashMap<String, String>) getIntent().getExtras().get("userMap");
        Uri imageUri = (Uri) getIntent().getExtras().get("imageUri");

        phoneInput = findViewById(R.id.editTextPhone);
        getOTPButton = findViewById(R.id.getOTPButton);

        progressBar = findViewById(R.id.progressBar);

        getOTPButton.setOnClickListener(view -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            progressBar.setVisibility(View.VISIBLE);
            getOTPButton.setVisibility(View.GONE);
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                getOTPButton.setVisibility(View.VISIBLE);
            } else if (!(phoneNumber.length() == 10)) {
                Toast.makeText(this, "Please enter valid number", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                getOTPButton.setVisibility(View.VISIBLE);
            } else {
                users = new ArrayList<>();
                usersReference.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;
                            users.add(user);
                        }
                        if (containsPhoneNumber(users, phoneNumber)) {
                            Toast.makeText(SendOTPActivity.this, "An account with this phone number already exists!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            getOTPButton.setVisibility(View.VISIBLE);
                        } else {
                            options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber("+91" + phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(SendOTPActivity.this)
                                    .setCallbacks(
                                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                @Override
                                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                                    progressBar.setVisibility(View.GONE);
                                                    getOTPButton.setVisibility(View.VISIBLE);
                                                    firebaseAuth.signInWithCredential(phoneAuthCredential);
                                                    Toast.makeText(SendOTPActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(SendOTPActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }

                                                @Override
                                                public void onVerificationFailed(@NonNull FirebaseException e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    getOTPButton.setVisibility(View.VISIBLE);
                                                    Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                                    super.onCodeSent(verificationId, forceResendingToken);
                                                    progressBar.setVisibility(View.GONE);
                                                    getOTPButton.setVisibility(View.VISIBLE);
                                                    verificationID = verificationId;
                                                    resendToken = forceResendingToken;

                                                    Intent intent = new Intent(SendOTPActivity.this, VerifyOTPActivity.class);
                                                    intent.putExtra("phone", phoneNumber);
                                                    intent.putExtra("userMap", map);
                                                    intent.putExtra("imageUri", imageUri);
                                                    intent.putExtra("verificationId", verificationID);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                    )
                                    .build();
                            PhoneAuthProvider.verifyPhoneNumber(options);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        });

    }
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean containsPhoneNumber(final ArrayList<User> userList, final String phoneNumber) {
        return userList.stream().anyMatch(user -> user.getPhone().equals(phoneNumber));
    }
}