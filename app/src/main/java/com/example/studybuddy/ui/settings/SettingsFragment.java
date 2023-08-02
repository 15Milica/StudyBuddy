package com.example.studybuddy.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.PinnedMessagesActivity;
import com.example.studybuddy.R;

public class SettingsFragment extends Fragment {

    private Switch notification;
    private Switch status;
    private LinearLayout linearLayoutPinnedMessages;
    private LinearLayout linearLayoutAbout;
    private LinearLayout linearLayoutPrivacyTerms;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        linearLayoutPinnedMessages = root.findViewById((R.id.linearLayoutPinnedMessagesSettings));
        linearLayoutAbout = root.findViewById(R.id.linearLayoutAboutSettings);
        linearLayoutPrivacyTerms = root.findViewById(R.id.linearLayoutPrivacyTermsSettings);


        linearLayoutPinnedMessages.setOnClickListener((view -> onPinnedMessage()));
        linearLayoutAbout.setOnClickListener(view -> onAbout());
        linearLayoutPrivacyTerms.setOnClickListener(view -> onPrivacyTerms());
        return root;
    }
    private void onPinnedMessage() {
        linearLayoutAbout.setEnabled(false);
        Intent intent = new Intent(getActivity(), PinnedMessagesActivity.class);
        startActivity(intent);
    }
    private void onAbout() {
        linearLayoutAbout.setEnabled(false);
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        startActivity(intent);
    }

    private void onPrivacyTerms() {
        linearLayoutPrivacyTerms.setEnabled(false);
        Intent intent = new Intent(getActivity(), PrivacyTermsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        linearLayoutAbout.setEnabled(true);
        linearLayoutPrivacyTerms.setEnabled(true);
    }

}