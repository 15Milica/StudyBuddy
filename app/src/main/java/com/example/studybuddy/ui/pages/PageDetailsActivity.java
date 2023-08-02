package com.example.studybuddy.ui.pages;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class PageDetailsActivity extends AppCompatActivity {

    private ImageButton newPageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_details);

        newPageButton = (ImageButton) findViewById(R.id.imageButtonBackDetails);
        newPageButton.setOnClickListener(view -> onBackPressed());
    }
}