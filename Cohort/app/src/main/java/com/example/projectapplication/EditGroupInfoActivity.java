package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class EditGroupInfoActivity extends AppCompatActivity {
    private TextView headingText;
    private EditText groupNameInput, groupDescriptionInput, searchTag1Input, searchTag2Input, searchTag3Input;
    private Button doneButton, deleteGroupButton;

    private DatabaseReference groupsReference;
    private String groupId;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_info);

        headingText = findViewById(R.id.headingText);
        groupNameInput = findViewById(R.id.editTextGroupName);
        groupDescriptionInput = findViewById(R.id.editTextGroupDescription);
        searchTag1Input = findViewById(R.id.editTextSearchTag1);
        searchTag2Input = findViewById(R.id.editTextSearchTag2);
        searchTag3Input = findViewById(R.id.editTextSearchTag3);
        doneButton = findViewById(R.id.doneButton);
        deleteGroupButton = findViewById(R.id.deleteGroupButton);

        groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

        doneButton.setOnClickListener(view -> {
            String groupName = groupNameInput.getText().toString().trim();
            String groupDescription = groupDescriptionInput.getText().toString().trim();
            String searchTag1 = searchTag1Input.getText().toString().trim();
            String searchTag2 = searchTag2Input.getText().toString().trim();
            String searchTag3 = searchTag3Input.getText().toString().trim();

            if (groupName.isEmpty() || groupDescription.isEmpty() || searchTag1.isEmpty() || searchTag2.isEmpty() || searchTag3.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            } else {
                group.setName(groupName);
                group.setDescription(groupDescription);
                ArrayList<String> searchTags = new ArrayList<>();
                searchTags.add(searchTag1);
                searchTags.add(searchTag2);
                searchTags.add(searchTag3);
                group.setSearchTags(searchTags);

                groupsReference.child(groupId).setValue(group).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Group information edited", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, com.example.projectapplication.GroupInfoActivity.class);
                        intent.putExtra("groupId", groupId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        deleteGroupButton.setOnClickListener(view -> deleteGroup(groupId));

    }

    private void loadGroupInfo() {
        groupsReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);
                assert group != null;
                headingText.setText("Edit " + group.getName());
                groupNameInput.setText(group.getName());
                groupDescriptionInput.setText(group.getDescription());
                searchTag1Input.setText(group.getSearchTags().get(0));
                searchTag2Input.setText(group.getSearchTags().get(1));
                searchTag3Input.setText(group.getSearchTags().get(2));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteGroup(String groupId) {
        groupsReference.child(groupId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}