package com.example.studybuddy.ui.inbox;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class InboxFragment extends Fragment {

    private ImageButton imageInboxGroup;
    private ImageButton imageInboxNewChat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);


        imageInboxGroup = (ImageButton) root.findViewById(R.id.imageInboxGroup);
        imageInboxNewChat =(ImageButton) root.findViewById(R.id.imageInboxNewChat);

        imageInboxGroup.setOnClickListener(view -> onClickInboxGroup());
        imageInboxNewChat.setOnClickListener((view -> onClickInboxNewChat()));


        return root;
    }
    private void onClickInboxGroup() {
        Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }
    private void onClickInboxNewChat() {
        Intent intent = new Intent(getActivity(), NewChatActivity.class);
        startActivity(intent);
    }
}