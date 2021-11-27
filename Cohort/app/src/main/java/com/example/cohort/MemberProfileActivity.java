package com.example.cohort;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

public class MemberProfileActivity extends AppCompatActivity {
    private TextView fullName, firstName, lastName, phone, email;
    private RoundedImageView memberProfileImage;
    private DatabaseReference databaseReference;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        fullName = findViewById(R.id.profileTextFullName);
        firstName = findViewById(R.id.profileTextFirstName);
        lastName = findViewById(R.id.profileTextLastName);
        phone = findViewById(R.id.profileTextPhone);
        email = findViewById(R.id.profileTextEmail);
        memberProfileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);

        String memberId = getIntent().getStringExtra("memberId");

        databaseReference.child(memberId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                com.example.cohort.User member = snapshot.getValue(com.example.cohort.User.class);
                assert member != null;
                fullName.setText(member.getFirstName() + "\n" + member.getLastName());
                firstName.setText("First name: " + member.getFirstName());
                lastName.setText("Last name: " + member.getLastName());
                phone.setText("Phone: " + member.getPhone());
                email.setText("Email: " + member.getEmail());

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(member.getImageUrl())
                        .into(memberProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        backButton.setOnClickListener(view -> finish());
    }
}