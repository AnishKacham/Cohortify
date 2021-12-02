package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;

public class UserProfileImageActivity extends AppCompatActivity {
    private RoundedImageView selectProfileImage;
    private Button nextButton;

    private Uri imageUri;

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_image);

        HashMap<String, String> map = (HashMap<String, String>) getIntent().getExtras().get("userMap");

        selectProfileImage = findViewById(R.id.selectProfileImage);
        nextButton = findViewById(R.id.nextButton);

        selectProfileImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(galleryIntent, 2);
            }
        });

        nextButton.setOnClickListener(view -> {
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, com.example.projectapplication.SendOTPActivity.class);
                intent.putExtra("userMap", map);
                intent.putExtra("imageUri", imageUri);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectProfileImage.setImageURI(imageUri);
        }
    }
}