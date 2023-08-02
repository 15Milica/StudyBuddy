package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

public class PinnedMessagesActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_messages);

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackPinnedMessage);
        imageButtonBack.setOnClickListener(view -> onBackPressed());
    }
}