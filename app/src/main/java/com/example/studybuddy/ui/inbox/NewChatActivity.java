package com.example.studybuddy.ui.inbox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class NewChatActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);


        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackNewChat);
        imageButtonBack.setOnClickListener(view -> onBackPressed());
    }
}