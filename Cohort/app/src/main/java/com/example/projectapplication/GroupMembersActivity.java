package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupMembersActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private com.example.projectapplication.Group group;
    private ArrayList<User> members;
    private RecyclerView membersRecyclerView;
    private TextView headingText;
    private Button viewHostDetailsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        headingText = findViewById(R.id.headingText);
        viewHostDetailsButton = findViewById(R.id.viewHostDetailsButton);

        String groupId = getIntent().getStringExtra("groupId");
        databaseReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(com.example.projectapplication.Group.class);
                assert group != null;
                headingText.setText("Members of " + group.getName());
                members = new ArrayList<>();
                if (group.getMembers() == null) {
                    group.setMembers(new ArrayList<>());
                }
                members.addAll(group.getMembers());
                MembersAdapter membersAdapter = new MembersAdapter(members, getApplicationContext(), GroupMembersActivity.this, group);
                membersRecyclerView.setAdapter(membersAdapter);

                if (group.getHostId().equals(FirebaseAuth.getInstance().getUid())) {
                    viewHostDetailsButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        viewHostDetailsButton.setOnClickListener(view -> {
            Intent intent = new Intent(GroupMembersActivity.this, MemberProfileActivity.class);
            intent.putExtra("memberId", group.getHostId());
            startActivity(intent);
        });
        
    }
}