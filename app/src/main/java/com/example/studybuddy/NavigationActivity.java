package com.example.studybuddy;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.HashMap;

public class NavigationActivity extends AppCompatActivity {

    private static FloatingActionButton buttonHome;
    private FirebaseUser firebaseUser;
    private SessionManager sessionManager;
    private boolean status;
    private DatabaseReference user_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        sessionManager = new SessionManager(getApplicationContext());
        user_status = FirebaseDatabase.getInstance().getReference("user_status").child(firebaseUser.getUid());

        buttonHome = (FloatingActionButton) findViewById(R.id.floatingActionButtonHome);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_navigation);
        NavigationUI.setupWithNavController(navView, navController);

        navView.getMenu().getItem(2).setEnabled(false);

        buttonHome.setOnClickListener(view -> {
            if(navView.getSelectedItemId() != R.id.navigation_home) { onBackPressed(); }
            navView.setSelectedItemId(R.id.navigation_home);
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) { return; }
                    updateToken(task.getResult());
                });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", token);
        reference.updateChildren(map);
    }

    public static void setButtonHomeActivated(boolean enabled) { buttonHome.setActivated(enabled); }

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
