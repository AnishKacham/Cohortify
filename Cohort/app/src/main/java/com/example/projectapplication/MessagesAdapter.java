package com.example.projectapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesHolder> {
    private ArrayList<com.example.projectapplication.Message> messages;
    private Context context;
    private Activity activity;

    private final static int MESSAGE_LEFT = 0, MESSAGE_RIGHT = 1, JOIN_LEAVE_MESSAGE = 2;

    public MessagesAdapter (ArrayList<com.example.projectapplication.Message> messages, Context context, Activity activity) {
        this.messages = messages;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MessagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MESSAGE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_left_message, parent, false);
        } else if (viewType == MESSAGE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_right_message, parent, false);
        }  else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_join_leave_message, parent, false);
        }
        return new MessagesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesHolder holder, int position) {
        if (messages.get(position).senderId != null) {
            DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
            userDatabaseReference.child(messages.get(position).senderId).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    com.example.projectapplication.User user = snapshot.getValue(com.example.projectapplication.User.class);

                    if (messages.get(position).senderId.equals(FirebaseAuth.getInstance().getUid())) {
                        holder.nameText.setText("You");

                    } else {
                        assert user != null;
                        holder.nameText.setText(user.getFirstName() + " " + user.getLastName());
                        Glide.with(context)
                                .asBitmap()
                                .load(user.getImageUrl())
                                .into(holder.userProfileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.messageText.setText(messages.get(position).message);

            holder.timestampText.setText(getDate(messages.get(position).timestamp));

            if (messages.get(position).isEdited) {
                holder.editedText.setVisibility(View.VISIBLE);
            }
            if (messages.get(position).senderId.equals(FirebaseAuth.getInstance().getUid())) {
                holder.messageContainer.setOnLongClickListener(view -> {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                    View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.layout_message_bottom_sheet, holder.messageBottomSheetContainer);

                    bottomSheetView.findViewById(R.id.deleteMsgButton).setOnClickListener(view1 -> {
                        deleteMessage(messages.get(position), position);
                        bottomSheetDialog.dismiss();
                    });
                    bottomSheetView.findViewById(R.id.editMsgButton).setOnClickListener(view1 -> {

                        com.example.projectapplication.EditMessageDialog editMessageDialog = new com.example.projectapplication.EditMessageDialog(messages.get(position), position, context);
                        editMessageDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "Edit Message");


                        bottomSheetDialog.dismiss();
                    });

                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                    return true;
                });
            }
        }
        else {
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
            userReference.child(messages.get(position).userId).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    com.example.projectapplication.User user = snapshot.getValue(com.example.projectapplication.User.class);
                    assert user != null;
                    if (messages.get(position).joinLeave.equals("joined")) {
                        holder.joinLeaveText.setText(user.getFirstName() + " " + user.getLastName() + " has joined the group");
                    } else if (messages.get(position).joinLeave.equals("left")) {
                        holder.joinLeaveText.setText(user.getFirstName() + " " + user.getLastName() + " has left the group");
                    } else {
                        holder.joinLeaveText.setText(user.getFirstName() + " " + user.getLastName() + " has been removed by the host");
                    }
                    holder.joinLeaveTimestamp.setText(getDate(messages.get(position).timestamp));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void deleteMessage(com.example.projectapplication.Message message, int position) {
        DatabaseReference messagesReference = FirebaseDatabase.getInstance().getReference("Group chats").child(message.groupId);
        messages.remove(position);
        messagesReference.setValue(messages).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show();
                notifyItemRemoved(position);
            } else {
                Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId != null) {
            if (messages.get(position).senderId.equals(FirebaseAuth.getInstance().getUid())) {
                return MESSAGE_RIGHT;
            } else {
                return MESSAGE_LEFT;
            }
        } else {
            return JOIN_LEAVE_MESSAGE;
        }
    }

    public class MessagesHolder extends RecyclerView.ViewHolder {
        private RoundedImageView userProfileImage;
        private TextView nameText, messageText, timestampText, joinLeaveText, joinLeaveTimestamp, editedText;
        private RelativeLayout messageContainer;
        private LinearLayout messageBottomSheetContainer, editMessageDialogContainer;

        public MessagesHolder(@NonNull View view) {
            super(view);

            userProfileImage = view.findViewById(R.id.userProfileImage);
            nameText = view.findViewById(R.id.nameText);
            messageText = view.findViewById(R.id.messageText);
            timestampText = view.findViewById(R.id.timestampText);
            joinLeaveText = view.findViewById(R.id.joinLeaveText);
            joinLeaveTimestamp = view.findViewById(R.id.joinLeaveTimeStamp);
            messageContainer = view.findViewById(R.id.messageContainer);
            messageBottomSheetContainer = view.findViewById(R.id.messageBottomSheetContainer);
            editMessageDialogContainer = view.findViewById(R.id.editMessageDialogContainer);
            editedText = view.findViewById(R.id.editedText);
        }
    }

    private String getDate(String timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp) * 1000);
        String date = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        return date;
    }
}
