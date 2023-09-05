package com.example.studybuddy.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AboutActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    private SessionManager sessionManager;
    private boolean status;
    private DatabaseReference user_status;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        sessionManager = new SessionManager(getApplicationContext());
        user_status = FirebaseDatabase.getInstance().getReference("users_status").child(firebaseUser.getUid());

        imageButtonBack=(ImageButton) findViewById(R.id.imageButtonBackAbout);
        imageButtonBack.setOnClickListener(view->onBackPressed());
    }
    @Override
    protected void onStart() {
        super.onStart();

        status = sessionManager.getActivityStatus();

        if(status) { user_status.onDisconnect().setValue("Offline"); }
        else {
            user_status.setValue("");
            user_status.onDisconnect().setValue("");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(status) { user_status.setValue("Online"); }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(status) { user_status.setValue("Offline"); }
    }
}