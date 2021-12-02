package com.example.projectapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText firstNameInput, lastNameInput, emailInput;
    private Button nextButton;

    private TextView alreadyHaveAccountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameInput = findViewById(R.id.editTextFirstName);
        lastNameInput = findViewById(R.id.editTextLastName);
        emailInput = findViewById(R.id.editTextEmail);
        nextButton = findViewById(R.id.nextButton);

        alreadyHaveAccountText = findViewById(R.id.alreadyHaveAccountText);

        alreadyHaveAccountText.setOnClickListener(view -> sendToLoginActivity());

        nextButton.setOnClickListener(view -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show();
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put("firstName", firstName);
                map.put("lastName", lastName);
                map.put("email", email);

                Intent intent = new Intent(this, com.example.projectapplication.UserProfileImageActivity.class);
                intent.putExtra("userMap", map);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        });

    }

    private void sendToLoginActivity() {
        startActivity(new Intent(this, com.example.projectapplication.LoginActivity.class));
    }

}