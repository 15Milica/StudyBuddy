package com.example.studybuddy.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.studybuddy.PinnedMessagesActivity;
import com.example.studybuddy.R;

public class ChatSettingsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private LinearLayout linearLayoutPinnedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackChatSettings);
        buttonBack.setOnClickListener(view -> onBackPressed());

        linearLayoutPinnedMessages = (LinearLayout) findViewById(R.id.linearLayoutPinnedMessagesSettings);
        linearLayoutPinnedMessages.setOnClickListener(view -> onClickPinnedMessages());
    }

    private void onClickPinnedMessages(){
        Intent intent = new Intent(this, PinnedMessagesActivity.class);
        startActivity(intent);
    }
}