package com.example.studybuddy;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.studybuddy.databinding.ActivityNavigationBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NavigationActivity extends AppCompatActivity {

    private static FloatingActionButton buttonHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        buttonHome = (FloatingActionButton) findViewById(R.id.floatingActionButtonHome);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_navigation);
        NavigationUI.setupWithNavController(navView, navController);

        navView.getMenu().getItem(2).setEnabled(false);

        buttonHome.setOnClickListener(view -> {
            if(navView.getSelectedItemId() != R.id.navigation_home) { onBackPressed(); }
            navView.setSelectedItemId(R.id.navigation_home);
        });
    }

    public static void setButtonHomeActivated(boolean enabled) { buttonHome.setActivated(enabled); }

}
