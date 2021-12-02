package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

public class AccountFragment extends Fragment {
    private TextView fullNameText, firstNameText, lastNameText, phoneText, emailText;
    private Button logoutButton;
    private RoundedImageView profileImage;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressBar progressBar;
    private LinearLayout userInfoLayout;

    private RelativeLayout userProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        fullNameText = view.findViewById(R.id.profileTextFullName);
        phoneText = view.findViewById(R.id.profileTextPhone);
        emailText = view.findViewById(R.id.profileTextEmail);
        profileImage = view.findViewById(R.id.profileImage);
        logoutButton = view.findViewById(R.id.logoutButton);

        userProfile = view.findViewById(R.id.userProfile);
        progressBar = view.findViewById(R.id.progressBar);
        userInfoLayout = view.findViewById(R.id.userInfoLayout);

        logoutButton.setOnClickListener(view1 -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(getContext(), com.example.projectapplication.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadUserInformation();

        return view;
    }

    private void loadUserInformation() {
        progressBar.setVisibility(View.VISIBLE);
        userInfoLayout.setVisibility(View.GONE);
        databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                com.example.projectapplication.User user = snapshot.getValue(com.example.projectapplication.User.class);
                String fullName = user.getFirstName() + " " + user.getLastName();
                String phone = user.getPhone();
                String email = user.getEmail();
                String imageUrl = user.getImageUrl();

                fullNameText.setText(fullName);
                phoneText.setText(phone);
                emailText.setText(email);

                if (getContext() != null) {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(imageUrl)
                            .into(profileImage);
                }

                progressBar.setVisibility(View.GONE);
                userInfoLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
