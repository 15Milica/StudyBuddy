package com.example.studybuddy.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.NavigationActivity;
import com.example.studybuddy.R;

public class HomeFragment extends Fragment {
    private ImageButton btnNewPost;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnNewPost = (ImageButton) root.findViewById(R.id.buttonHomeNewPost);
        btnNewPost.setOnClickListener(view -> onClickNewPost());

        return root;
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