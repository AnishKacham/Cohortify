package com.example.projectapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private TextView needRegistrationText;

    private EditText phoneInput;
    private Button getOTPButton;

    private String verificationID;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private ProgressBar progressBar;

    private ArrayList<User> users;

    private DatabaseReference usersReference;

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        usersReference = FirebaseDatabase.getInstance().getReference("Users");

        needRegistrationText = findViewById(R.id.needRegistrationText);
        needRegistrationText.setOnClickListener(view -> sendToRegisterActivity());

        phoneInput = findViewById(R.id.editTextPhone);
        getOTPButton = findViewById(R.id.getOTPButton);
        progressBar = findViewById(R.id.progressBar);

        getOTPButton.setOnClickListener(view -> {
            String phoneNumber = phoneInput.getText().toString().trim();

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
                        if (!containsPhoneNumber(users, phoneNumber)) {
                            Toast.makeText(LoginActivity.this, "No account with this phone number exists!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            getOTPButton.setVisibility(View.VISIBLE);
                        } else {
                            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber("+91" + phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(LoginActivity.this)
                                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                            progressBar.setVisibility(View.GONE);
                                            getOTPButton.setVisibility(View.VISIBLE);
                                            firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(LoginActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            progressBar.setVisibility(View.GONE);
                                            getOTPButton.setVisibility(View.VISIBLE);
                                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            super.onCodeSent(verificationId, forceResendingToken);
                                            progressBar.setVisibility(View.GONE);
                                            getOTPButton.setVisibility(View.VISIBLE);
                                            verificationID = verificationId;
                                            resendToken = forceResendingToken;

                                            Intent intent = new Intent(LoginActivity.this, com.example.projectapplication.LoginVerifyOTPActivity.class);
                                            intent.putExtra("phone", phoneNumber);
                                            intent.putExtra("verificationId", verificationID);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    })
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

    private void sendToRegisterActivity() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean containsPhoneNumber(final ArrayList<User> userList, final String phoneNumber) {
        return userList.stream().anyMatch(user -> user.getPhone().equals(phoneNumber));
    }
}