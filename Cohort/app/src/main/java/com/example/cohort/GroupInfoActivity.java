package com.example.cohort;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity {
    private ImageView groupImage;
    private TextView headingGroupName, groupName, groupDescription, groupHostName, numberOfGroupMembers, searchTag1, searchTag2, searchTag3;
    private Button viewMembersButton, joinLeaveButton, editGroupInfoButton;

    private Group group;
    private String groupId, hostName;

    private User currentUser;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseReference;

    private ArrayList<Message> groupMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        firebaseAuth = FirebaseAuth.getInstance();
        userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");

        userDatabaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        groupId = getIntent().getStringExtra("groupId");
        hostName = getIntent().getStringExtra("hostName");

        groupImage = findViewById(R.id.groupImage);
        groupName = findViewById(R.id.groupName);
        headingGroupName = findViewById(R.id.headingGroupName);
        groupDescription = findViewById(R.id.groupDescription);
        groupHostName = findViewById(R.id.groupHostName);
        numberOfGroupMembers = findViewById(R.id.numberOfGroupMembers);
        searchTag1 = findViewById(R.id.searchTag1);
        searchTag2 = findViewById(R.id.searchTag2);
        searchTag3 = findViewById(R.id.searchTag3);

        viewMembersButton = findViewById(R.id.viewMembersButton);
        joinLeaveButton = findViewById(R.id.joinLeaveButton);
        editGroupInfoButton = findViewById(R.id.editGroupInfoButton);

        loadGroupInformation();
        loadGroupMessages();

        joinLeaveButton.setOnClickListener(view ->  {
            if (joinLeaveButton.getText().toString().toLowerCase().equals("join group")) {
                joinGroup();
            } else {
                leaveGroup();
            }
        });

        viewMembersButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, GroupMembersActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

        editGroupInfoButton.setOnClickListener(view ->  {
            Intent intent = new Intent(this, EditGroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        });

    }

    private void loadGroupMessages() {
        DatabaseReference messagesReference = FirebaseDatabase.getInstance().getReference("Group chats");
        messagesReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (groupMessages == null) {
                        groupMessages = new ArrayList<>();
                    }
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Message message = dataSnapshot.getValue(Message.class);
                        groupMessages.add(message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadGroupInformation() {

        DatabaseReference groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        groupDatabaseReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                group = snapshot.getValue(Group.class);

                assert group != null;
                headingGroupName.setText(group.getName());

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(group.getImageUrl())
                        .into(groupImage);

                groupName.setText(group.getName());
                groupDescription.setText(group.getDescription());
                groupHostName.setText(hostName);

                if (group.getHostId().equals(firebaseAuth.getUid())) {
                    joinLeaveButton.setVisibility(View.GONE);
                    editGroupInfoButton.setVisibility(View.VISIBLE);

                    if (group.getMembers() != null) {
                        viewMembersButton.setVisibility(View.VISIBLE);
                    }
                }

                if (group.getMembers() == null) {
                    numberOfGroupMembers.setText("Members: 0");
                } else {
                    numberOfGroupMembers.setText("Members: " + group.getMembers().size());
                    for (User user : group.getMembers()) {
                        if (user.getId().equals(firebaseAuth.getUid())) {
                            joinLeaveButton.setText("Leave Group");
                            viewMembersButton.setVisibility(View.VISIBLE);
                        }
                    }
                }
                searchTag1.setText(group.getSearchTags().get(0));
                searchTag2.setText(group.getSearchTags().get(1));
                searchTag3.setText(group.getSearchTags().get(2));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void joinGroup() {
        if (group.getMembers() == null) {
            group.setMembers(new ArrayList<>());
        }
        if (groupMessages == null) {
            groupMessages = new ArrayList<>();
        }
        group.getMembers().add(currentUser);
        DatabaseReference groupDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        groupDatabaseReference.child(groupId).child("members").setValue(group.getMembers()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = Long.toString(tsLong);
                Message message = new Message(FirebaseAuth.getInstance().getUid(), groupId, "joined", timestamp);
                groupMessages.add(message);

                DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Group chats");
                chatReference.child(groupId).setValue(groupMessages).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "Joined group successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupInfoActivity.this, GroupChatActivity.class);
                        intent.putExtra("groupId", groupId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void leaveGroup() {
        int position = 0;
        for (int i = 0; i < group.getMembers().size(); i++) {
            if (group.getMembers().get(i).getId().equals(firebaseAuth.getUid())) {
                position = i;
                break;
            }
        }

        group.getMembers().remove(position);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        databaseReference.child(groupId).child("members").setValue(group.getMembers()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = Long.toString(tsLong);
                Message message = new Message(FirebaseAuth.getInstance().getUid(), groupId, "left", timestamp);

                groupMessages.add(message);

                DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Group chats");
                chatReference.child(groupId).setValue(groupMessages).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(GroupInfoActivity.this, "You've left this group", Toast.LENGTH_SHORT).show();
                        joinLeaveButton.setText("join group");
                        Intent intent = new Intent(GroupInfoActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(GroupInfoActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        /*
        Query leaveQuery = databaseReference.child(groupId).child("members").orderByChild("id").equalTo(firebaseAuth.getUid());
        leaveQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            long tsLong = System.currentTimeMillis() / 1000;
                            String timestamp = Long.toString(tsLong);
                            Message message = new Message(FirebaseAuth.getInstance().getUid(), groupId, "left", timestamp);

                            groupMessages.add(message);

                            DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Group chats");
                            chatReference.child(groupId).setValue(groupMessages).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(GroupInfoActivity.this, "You've left this group", Toast.LENGTH_SHORT).show();
                                    joinLeaveButton.setText("join group");
                                    Intent intent = new Intent(GroupInfoActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(GroupInfoActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(GroupInfoActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
*/
    }
}