package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    private ArrayList<Group> groups;
    private Context context;

    public GroupsAdapter(ArrayList<Group> groups, Context context) {
        this.groups = groups;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_item, parent, false);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsViewHolder holder, int position) {
        Group group = groups.get(position);
        Glide.with(context)
                .asBitmap()
                .load(group.getImageUrl())
                .into(holder.groupImage);

        holder.groupName.setText(group.getName());
        holder.t1.setText(group.getSearchTags().get(0));
        holder.t2.setText(group.getSearchTags().get(1));
        holder.t3.setText(group.getSearchTags().get(2));
        holder.groupDesc.setText(group.getDescription());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;

                if (group.getHostId().equals(user.getId())) {
                    holder.groupHostName.setText("Host: You");
                } else {
                    databaseReference.child(group.getHostId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User host = snapshot.getValue(User.class);
                            assert host != null;
                            holder.groupHostName.setText("Host: " + host.getFirstName() + " " + host.getLastName());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.groupLayout.setOnClickListener(view -> {
            if (group.getHostId().equals(FirebaseAuth.getInstance().getUid())) {
                Intent intent = new Intent(context, com.example.projectapplication.GroupChatActivity.class);
                intent.putExtra("groupId", group.getId());
                intent.putExtra("hostName", holder.groupHostName.getText().toString());
                context.startActivity(intent);
            }
            else if (group.getMembers() != null) {
                for (User user : group.getMembers()) {
                    if (user.getId().equals(FirebaseAuth.getInstance().getUid())) {
                        Intent intent = new Intent(context, com.example.projectapplication.GroupChatActivity.class);
                        intent.putExtra("groupId", group.getId());
                        intent.putExtra("hostName", holder.groupHostName.getText().toString());
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, com.example.projectapplication.GroupInfoActivity.class);
                        intent.putExtra("groupId", group.getId());
                        intent.putExtra("hostName", holder.groupHostName.getText().toString());
                        context.startActivity(intent);
                    }
                }
            } else {
                Intent intent = new Intent(context, com.example.projectapplication.GroupInfoActivity.class);
                intent.putExtra("groupId", group.getId());
                intent.putExtra("hostName", holder.groupHostName.getText().toString());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView groupImage;
        private TextView groupName, groupDesc, groupHostName;
        private CardView groupLayout;
        private TextView t1,t2,t3;

        public GroupsViewHolder(@NonNull View view) {
            super(view);
            t1 = view.findViewById(R.id.tag1);
            t2 = view.findViewById(R.id.tag2);
            t3 = view.findViewById(R.id.tag3);
            groupImage = view.findViewById(R.id.groupImage);
            groupName = view.findViewById(R.id.groupName);
            groupDesc = view.findViewById(R.id.groupDesc);
            groupHostName = view.findViewById(R.id.groupHostName);
            groupLayout = view.findViewById(R.id.groupLayout);
        }
    }

}
