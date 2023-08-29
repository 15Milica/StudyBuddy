package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.studybuddy.NavigationActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.PostAdapter;


public class HomeFragment extends Fragment {
    private ImageButton btnNewPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnNewPost = (ImageButton) root.findViewById(R.id.buttonHomeNewPost);
        btnNewPost.setOnClickListener(view -> onClickNewPost());

        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayoutHome);
        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(this::SetPosts);
        SetPosts();

        return root;
    }
    private void SetPosts(){
        swipeRefreshLayout.setRefreshing(false);

       // postAdapter = new PostAdapter(getContext(), getActivity(), posts);
        recyclerView.clearOnScrollListeners();

        recyclerView.setAdapter(postAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationActivity.setButtonHomeActivated(true);
        btnNewPost.setEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        NavigationActivity.setButtonHomeActivated(false);
    }
    private void onClickNewPost(){
        btnNewPost.setEnabled(false);
        Intent intent = new Intent(getActivity(), NewPostActivity.class);
        startActivity(intent);

    }
}