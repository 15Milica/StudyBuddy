package com.example.studybuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.studybuddy.chat.ChatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.studybuddy.databinding.ActivityNavigationBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class NavigationActivity extends AppCompatActivity {

    private static final int PERMISSION_POST_NOTIFICATIONS = 100;
    private static FloatingActionButton buttonHome;
    private FirebaseUser firebaseUser;
    //private static String notificationsPermission = Manifest.permission.POST_NOTIFICATIONS;
    //private static int PERMISSION_CODE = 13;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        sessionManager = new SessionManager(getApplicationContext());

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

    /*private boolean checkPermission() {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), notificationsPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{notificationsPermission}, PERMISSION_CODE);
            return false;
        }
    }*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_POST_NOTIFICATIONS ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showContacts();
        }
    }
    private void showContacts() {
        if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_POST_NOTIFICATIONS);
        } else {
           // getContacts();
        }
    }
}
