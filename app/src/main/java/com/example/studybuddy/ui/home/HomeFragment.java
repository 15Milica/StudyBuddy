package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.studybuddy.NavigationActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.example.studybuddy.adapter.PostAdapter;
import com.example.studybuddy.algorithm.Algorithm;
import com.example.studybuddy.algorithm.OnCompleteListener;
import com.example.studybuddy.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


public class HomeFragment extends Fragment {
    private ImageButton btnNewPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private boolean status;
    private SessionManager sessionManager;
    private DatabaseReference user_status;
    private FirebaseUser firebaseUser;

    private Algorithm algorithm;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sessionManager = new SessionManager(getContext());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user_status = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());

        btnNewPost = (ImageButton) root.findViewById(R.id.buttonHomeNewPost);
        btnNewPost.setOnClickListener(view -> onClickNewPost());

        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayoutHome);
        swipeRefreshLayout.setRefreshing(true);

        algorithm = new Algorithm();

        swipeRefreshLayout.setOnRefreshListener(this::SetPosts);

        SetPosts();

        return root;
    }

    private void SetPosts() {
        algorithm.setPosts(posts -> {
            swipeRefreshLayout.setRefreshing(false);

            postAdapter = new PostAdapter(getContext(), getActivity(), posts);
            recyclerView.clearOnScrollListeners();
            recyclerView.setAdapter(postAdapter);
        });
    }

    private void onClickNewPost(){
        btnNewPost.setEnabled(false);
        Intent intent = new Intent(getActivity(), NewPostActivity.class);
        startActivity(intent);
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

}