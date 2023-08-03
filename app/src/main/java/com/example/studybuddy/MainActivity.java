package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studybuddy.chat.ChatActivity;
import com.example.studybuddy.ui.pages.PageActivity;
import com.example.studybuddy.ui.pages.PageDetailsActivity;
import com.example.studybuddy.ui.settings.AboutActivity;
import com.example.studybuddy.ui.settings.PrivacyTermsActivity;
import com.example.studybuddy.ui.settings.SettingsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finishAffinity();
    }




}