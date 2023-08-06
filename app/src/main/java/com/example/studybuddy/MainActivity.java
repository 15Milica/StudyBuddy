package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studybuddy.chat.ChatActivity;
import com.example.studybuddy.model.User;
import com.example.studybuddy.ui.pages.PageActivity;
import com.example.studybuddy.ui.pages.PageDetailsActivity;
import com.example.studybuddy.ui.settings.AboutActivity;
import com.example.studybuddy.ui.settings.PrivacyTermsActivity;
import com.example.studybuddy.ui.settings.SettingsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TotpSecret;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finishAffinity();
        } else {
            DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("users");

            profileRef.child(user.getUid()).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    User u = task.getResult().getValue(User.class);
                    if(u == null) {
                        Intent intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}