package com.example.studybuddy.ui.pages;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.studybuddy.R;
import com.example.studybuddy.adapter.PageAdapter;
import com.example.studybuddy.model.Page;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PagesFragment extends Fragment {

    private ImageButton newPageButton;
    private SwipeableRecyclerView recyclerView;
    private EditText search;
    private ProgressBar progressBar;
    private List<Page> pages;
    private FirebaseUser user;
    private DatabaseReference refDatabase;
    private PageAdapter pageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pages, container, false);

        pages = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        refDatabase = FirebaseDatabase.getInstance().getReference("pages");

        progressBar = (ProgressBar) root.findViewById(R.id.progressBarPages);

        newPageButton = (ImageButton) root.findViewById(R.id.imageButtonNewPage);
        newPageButton.setOnClickListener(view->onNewPageClick());

        recyclerView = (SwipeableRecyclerView) root.findViewById(R.id.recyclerViewPages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search = (EditText) root.findViewById(R.id.editTextPagesSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SearchPages(search.getText().toString().toLowerCase());
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        getPages();

        return root;
    }
    private void onNewPageClick() {
        Intent intent = new Intent(getContext(), NewPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    private void getPages(){
        progressBar.setVisibility(View.VISIBLE);
        refDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(search.getText().toString().isEmpty()) {
                    pages.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        pages.add(dataSnapshot.getValue(Page.class));
                    }
                    pageAdapter = new PageAdapter(getContext(), pages);
                    recyclerView.setAdapter(pageAdapter);
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }
    private void SearchPages(String text){
        progressBar.setVisibility(View.VISIBLE);

        Query query = FirebaseDatabase.getInstance().getReference("pages").orderByChild("search")
                .startAt(text)
                .endAt(text+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pages.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Page page = dataSnapshot.getValue(Page.class);
                    pages.add(page);
                }
                pageAdapter = new PageAdapter(getContext(), pages);
                recyclerView.setAdapter(pageAdapter);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }

}