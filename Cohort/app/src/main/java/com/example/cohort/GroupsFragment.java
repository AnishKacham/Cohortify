package com.example.cohort;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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

public class GroupsFragment extends Fragment {
    private ImageView createGroupImage;
    private RecyclerView groupsRecyclerView;
    private com.example.cohort.GroupsAdapter groupsAdapter;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ArrayList<com.example.cohort.Group> groups, searchGroups;

    private ProgressBar progressBar;

    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        createGroupImage = view.findViewById(R.id.createGroupImage);

        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView);

        progressBar = view.findViewById(R.id.progressBar);

        toolbar = view.findViewById(R.id.groupsToolbar);
        toolbar.setTitle("Dashboard");
        toolbar.setTitleTextColor(Color.BLACK);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        createGroupImage.setOnClickListener(view1 -> startActivity(new Intent(getContext(), CreateGroupActivity.class)));

        loadGroups();

        setHasOptionsMenu(true);

        return view;
    }

    private void loadGroups() {
        progressBar.setVisibility(View.VISIBLE);
        groups = new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        com.example.cohort.Group group = dataSnapshot.getValue(com.example.cohort.Group.class);
                        assert group != null;
                        groups.add(group);
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    groupsRecyclerView.setLayoutManager(layoutManager);
                    groupsAdapter = new com.example.cohort.GroupsAdapter(groups, getContext());
                    groupsRecyclerView.setAdapter(groupsAdapter);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search groups");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                searchGroup(s);

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchGroup(String searchInput) {
        searchGroups = new ArrayList<>();
        for (com.example.cohort.Group group : groups) {
            String tag1 = group.getSearchTags().get(0);
            String tag2 = group.getSearchTags().get(1);
            String tag3 = group.getSearchTags().get(2);

            if (tag1.toLowerCase().contains(searchInput.toLowerCase()) || tag2.toLowerCase().contains(searchInput.toLowerCase()) || tag3.toLowerCase().contains(searchInput.toLowerCase()) || group.getName().toLowerCase().contains(searchInput.toLowerCase())) {
                searchGroups.add(group);
            }
        }
        groupsAdapter = new com.example.cohort.GroupsAdapter(searchGroups, getContext());
        groupsRecyclerView.setAdapter(groupsAdapter);
    }
}
