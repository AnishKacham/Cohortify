package com.example.projectapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private ImageView sendMessageImage;

    private TextView groupName;
    private RoundedImageView groupImage;
    private LinearLayout groupInfoLayout;

    private String groupId, hostName;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference groupsDatabaseReference, groupChatsDatabaseReference;

    private ArrayList<Message> messages;
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        groupsDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        groupChatsDatabaseReference = FirebaseDatabase.getInstance().getReference("Group chats");

        groupId = getIntent().getStringExtra("groupId");
        hostName = getIntent().getStringExtra("hostName");

        groupName = findViewById(R.id.groupName);
        groupImage = findViewById(R.id.groupImage);
        groupInfoLayout = findViewById(R.id.groupInfoLayout);

        messageInput = findViewById(R.id.editTextMessage);
        sendMessageImage = findViewById(R.id.sendMessageImage);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        loadGroup();
        loadMessages();

        groupInfoLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, com.example.projectapplication.GroupInfoActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("hostName", hostName);
            startActivity(intent);
        });

        sendMessageImage.setOnClickListener(view -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Type a message to continue", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(message);
            }
        });


    }

    private void loadGroup() {
        groupsDatabaseReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Group group = snapshot.getValue(Group.class);
                assert group != null;

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(group.getImageUrl())
                        .into(groupImage);

                groupName.setText(group.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String messageToSend) {
        String messageId = groupsDatabaseReference.child(groupId).push().getKey();
        long tsLong = System.currentTimeMillis() / 1000;
        String timestamp = Long.toString(tsLong);
        Message message = new Message(messageId, firebaseAuth.getUid(), groupId, timestamp, messageToSend);

        messages.add(message);

        assert messageId != null;
        groupChatsDatabaseReference.child(groupId).setValue(messages).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
                loadMessages();
            } else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        groupChatsDatabaseReference.child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Group group = snapshot.getValue(Group.class);

                    assert group != null;
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(group.getImageUrl())
                            .into(groupImage);

                    groupName.setText(group.getName());
                } else {
                    Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}