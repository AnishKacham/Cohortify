package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class GroupImageActivity extends AppCompatActivity {
    private ImageView selectGroupImage;
    private Button createGroupButton;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressBar progressBar;

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_image);

        HashMap<String, String> map = (HashMap<String, String>) getIntent().getExtras().get("groupMap");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        storageReference = FirebaseStorage.getInstance().getReference("Group images");

        selectGroupImage = findViewById(R.id.selectGroupImage);
        createGroupButton = findViewById(R.id.createGroupButton);
        progressBar = findViewById(R.id.progressBar);

        selectGroupImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            if (galleryIntent.resolveActivity(getPackageManager()) != null) {
                galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(galleryIntent, 2);
            }
        });

        createGroupButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            createGroupButton.setVisibility(View.GONE);
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                createGroupButton.setVisibility(View.VISIBLE);
            } else {
                StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
                UploadTask uploadTask = fileReference.putFile(imageUri);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        createGroupButton.setVisibility(View.VISIBLE);
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (!(downloadUri == null)) {
                            String imagePath = downloadUri.toString();
                            String groupId = databaseReference.push().getKey();
                            String hostId = firebaseAuth.getUid();
                            String name = map.get("name");
                            String description = map.get("description");
                            ArrayList<String> searchTags = new ArrayList<>();
                            searchTags.add(map.get("searchTag1"));
                            searchTags.add(map.get("searchTag2"));
                            searchTags.add(map.get("searchTag3"));

                            com.example.projectapplication.Group group = new com.example.projectapplication.Group(groupId, hostId, name, description, imagePath, null, searchTags);

                            assert groupId != null;
                            databaseReference.child(groupId).setValue(group).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    createGroupButton.setVisibility(View.VISIBLE);
                                    Intent intent = new Intent(this, com.example.projectapplication.MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    createGroupButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        createGroupButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectGroupImage.setImageURI(imageUri);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));

    }
}