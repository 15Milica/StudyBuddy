package com.example.studybuddy.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class PrivacyTermsActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_terms);

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackPrivacyTerms);
        imageButtonBack.setOnClickListener(view -> onBackPressed());
    }
}