package com.example.cohort;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder> {
    ArrayList<User> members;
    Context context;
    Activity activity;
    com.example.cohort.Group group;

    ArrayList<Message> messages;

    public MembersAdapter (ArrayList<User> members, Context context, Activity activity, com.example.cohort.Group group) {
        this.members = members;
        this.context = context;
        this.activity = activity;
        this.group = group;
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_member, parent, false);
        return new MembersViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        getMessages();
        Glide.with(context)
                .asBitmap()
                .load(members.get(position).getImageUrl())
                .into(holder.userProfileImage);

        if (members.get(position).getId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.userFullName.setText("You");
        } else {
            holder.userFullName.setText(members.get(position).getFirstName() + " " + members.get(position).getLastName());
        }



        holder.memberLayout.setOnClickListener(view -> {
            if (members.get(position).getId().equals(FirebaseAuth.getInstance().getUid())) {
                Toast.makeText(context, "You're a member of this group", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(context, MemberProfileActivity.class);
                intent.putExtra("memberId", members.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        if (group.getHostId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.memberLayout.setOnLongClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(activity, holder.memberLayout);
                popupMenu.getMenuInflater().inflate(R.menu.remove_member_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    removeMember(members.get(position));
                    return true;
                });
                popupMenu.show();
                return true;
            });
        }

    }

    private void removeMember(User member) {
        members.remove(member);
        DatabaseReference membersReference = FirebaseDatabase.getInstance().getReference("Groups").child(group.getId());
        membersReference.child("members").setValue(members).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long ts = System.currentTimeMillis() / 1000;
                String timestamp = Long.toString(ts);
                Message message = new Message(member.getId(), group.getId(), "removed", timestamp);
                messages.add(message);

                DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Group chats");
                chatReference.child(group.getId()).setValue(messages).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(context, "Removed " + member.getFirstName() + " " + member.getLastName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMessages() {
        DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference("Group chats");
        chatsReference.child(group.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MembersViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView userProfileImage;
        TextView userFullName;
        CardView memberLayout;
        public MembersViewHolder(View view) {
            super(view);

            userProfileImage = view.findViewById(R.id.userProfileImage);
            userFullName = view.findViewById(R.id.userFullName);
            memberLayout = view.findViewById(R.id.memberLayout);
        }
    }
}
