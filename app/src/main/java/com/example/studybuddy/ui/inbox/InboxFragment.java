package com.example.studybuddy.ui.inbox;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;

public class InboxFragment extends Fragment {

    private ImageButton imageInboxGroup;
    private ImageButton imageInboxNewChat;
    private EditText search;
    private SwipeableRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);

        imageInboxGroup = (ImageButton) root.findViewById(R.id.imageInboxGroup);
        imageInboxNewChat =(ImageButton) root.findViewById(R.id.imageInboxNewChat);

        imageInboxGroup.setOnClickListener(view -> onClickInboxGroup());
        imageInboxNewChat.setOnClickListener((view -> onClickInboxNewChat()));

        search = (EditText) root.findViewById(R.id.editTextInboxSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchChat(search.getText().toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        recyclerView = (SwipeableRecyclerView) root.findViewById(R.id.recyclerViewInbox);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return root;
    }
    private void onClickInboxGroup() {
        imageInboxGroup.setEnabled(false);
        Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
        startActivity(intent);
    }
    private void onClickInboxNewChat() {
        imageInboxNewChat.setEnabled(false);
        Intent intent = new Intent(getActivity(), NewChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        imageInboxNewChat.setEnabled(true);
        imageInboxGroup.setEnabled(true);
    }

    private void searchChat(String text){

    }

}