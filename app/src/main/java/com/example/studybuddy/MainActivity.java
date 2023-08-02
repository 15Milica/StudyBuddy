package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.studybuddy.ui.pages.PageActivity;
import com.example.studybuddy.ui.pages.PageDetailsActivity;
import com.example.studybuddy.ui.settings.AboutActivity;
import com.example.studybuddy.ui.settings.PrivacyTermsActivity;
import com.example.studybuddy.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
        finishAffinity();
    }




}