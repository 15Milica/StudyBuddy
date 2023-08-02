package com.example.studybuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.NavigationActivity;
import com.example.studybuddy.R;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        NavigationActivity.setButtonHomeActivated(true);
    }

    @Override
    public void onStop() {
        super.onStop();

        NavigationActivity.setButtonHomeActivated(false);
    }
}