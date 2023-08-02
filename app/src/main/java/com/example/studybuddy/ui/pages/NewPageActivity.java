package com.example.studybuddy.ui.pages;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class NewPageActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_page);

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackNewPage);
        imageButtonBack.setOnClickListener(view->onBackPressed());
    }
}