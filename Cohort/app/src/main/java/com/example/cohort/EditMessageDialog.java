package com.example.cohort;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class EditMessageDialog extends AppCompatDialogFragment {

    private com.example.cohort.Message message;
    private int position;
    private Context context;

    public EditMessageDialog(com.example.cohort.Message message, int position, Context context) {
        this.message = message;
        this.position = position;
        this.context = context;
    }

    EditText editMessageInput;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editMessageDialogView = inflater.inflate(R.layout.layout_edit_message_dialog, null);
        builder.setView(editMessageDialogView);
        editMessageDialogView.findViewById(R.id.cancelText).setOnClickListener(view -> {
            dismiss();
        });
        editMessageInput = editMessageDialogView.findViewById(R.id.editMessageInput);
        editMessageInput.setText(message.message);
        editMessageInput.requestFocus();

        editMessageDialogView.findViewById(R.id.editMessageText).setOnClickListener(view -> {
            String editedMessage = editMessageInput.getText().toString().trim();

            if (editedMessage.isEmpty()) {
                Toast.makeText(context, "Edited message cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                message.message = editedMessage;
                message.isEdited = true;
                DatabaseReference messagesReference = FirebaseDatabase.getInstance().getReference("Group chats");
                messagesReference.child(message.groupId).child(String.valueOf(position)).setValue(message).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Message edited", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                dismiss();
            }

        });

        return builder.create();
    }
}
