package com.example.cohort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGroupsFragment extends Fragment {
    private DatabaseReference groupsReference;
    private RecyclerView myGroupsRecyclerView;
    private com.example.cohort.GroupsAdapter myGroupsAdapter;
    private ArrayList<com.example.cohort.Group> myGroups;
    private User currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_groups, container, false);
        groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        myGroupsRecyclerView = view.findViewById(R.id.myGroupsRecyclerview);
        loadGroups();
        return view;
    }

    private void loadGroups() {
        myGroups = new ArrayList<>();
        groupsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        com.example.cohort.Group group = dataSnapshot.getValue(com.example.cohort.Group.class);
                        assert group != null;

                        if (group.getMembers() == null) {
                            group.setMembers(new ArrayList<>());
                        }
                        if(group.getHostId().equals(FirebaseAuth.getInstance().getUid())) {
                            myGroups.add(group);
                        }
                        for (User user : group.getMembers()) {
                            if (user.getId().equals(FirebaseAuth.getInstance().getUid())) {
                                myGroups.add(group);
                            }
                        }


                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    myGroupsRecyclerView.setLayoutManager(layoutManager);
                    myGroupsAdapter = new com.example.cohort.GroupsAdapter(myGroups, getContext());
                    myGroupsRecyclerView.setAdapter(myGroupsAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}