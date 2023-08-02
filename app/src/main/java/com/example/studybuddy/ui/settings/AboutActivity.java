package com.example.studybuddy.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class AboutActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        imageButtonBack=(ImageButton) findViewById(R.id.imageButtonBackAbout);
        imageButtonBack.setOnClickListener(view->onBackPressed());
    }
}