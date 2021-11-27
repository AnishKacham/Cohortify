package com.example.cohort;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText groupNameInput, groupDescriptionInput, searchTag1Input, searchTag2Input, searchTag3Input;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupNameInput = findViewById(R.id.editTextGroupName);
        groupDescriptionInput = findViewById(R.id.editTextGroupDescription);
        searchTag1Input = findViewById(R.id.editTextSearchTag1);
        searchTag2Input = findViewById(R.id.editTextSearchTag2);
        searchTag3Input = findViewById(R.id.editTextSearchTag3);

        nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(view -> {
            String groupName = groupNameInput.getText().toString().trim();
            String groupDescription = groupDescriptionInput.getText().toString().trim();
            String searchTag1 = searchTag1Input.getText().toString().trim();
            String searchTag2 = searchTag2Input.getText().toString().trim();
            String searchTag3 = searchTag3Input.getText().toString().trim();

            if (groupName.isEmpty() || groupDescription.isEmpty() || searchTag1.isEmpty() || searchTag2.isEmpty() || searchTag3.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                HashMap<String, String> groupMap = new HashMap<>();
                groupMap.put("name", groupName);
                groupMap.put("description", groupDescription);
                groupMap.put("searchTag1", searchTag1);
                groupMap.put("searchTag2", searchTag2);
                groupMap.put("searchTag3", searchTag3);

                Intent intent = new Intent(this, com.example.cohort.GroupImageActivity.class);
                intent.putExtra("groupMap", groupMap);
                startActivity(intent);
            }

        });

    }
}