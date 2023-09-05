package com.example.studybuddy.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.studybuddy.PinnedMessagesActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment {

    private LinearLayout notification;
    private Switch status;
    private LinearLayout linearLayoutPinnedMessages;
    private LinearLayout linearLayoutAbout;
    private LinearLayout linearLayoutPrivacyTerms;
    private SessionManager sessionManager;
    private String[] permission= new String[] {Manifest.permission.POST_NOTIFICATIONS};

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        sessionManager = new SessionManager(getContext());
        notification = root.findViewById(R.id.linearLayoutSettingsNotifications);

        status = root.findViewById(R.id.switchActivitySettings);
        boolean s = sessionManager.getActivityStatus();
        status.setChecked(s);
        sessionManager.setNotification(false);

        notification.setOnClickListener(view -> {
            if(!sessionManager.getNotification()){
                requestPermissionNotification();
            }else {
                settingsDialog();
            }
        });

        status.setOnClickListener(view -> {
            boolean b = sessionManager.getActivityStatus();
            sessionManager.setActivityStatus(!b);
        });

        linearLayoutPinnedMessages = root.findViewById((R.id.linearLayoutPinnedMessagesSettings));
        linearLayoutAbout = root.findViewById(R.id.linearLayoutAboutSettings);
        linearLayoutPrivacyTerms = root.findViewById(R.id.linearLayoutPrivacyTermsSettings);

        linearLayoutPinnedMessages.setOnClickListener((view -> onPinnedMessage()));
        linearLayoutAbout.setOnClickListener(view -> onAbout());
        linearLayoutPrivacyTerms.setOnClickListener(view -> onPrivacyTerms());
        return root;
    }
    private void requestPermissionNotification(){
        if(ContextCompat.checkSelfPermission(getContext(), permission[0]) == PackageManager.PERMISSION_GRANTED) {
            sessionManager.setNotification(true);
        } else {
            requestPermissionLauncher.launch(permission[0]);
        }
    }
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
                if(isGranted){
                    sessionManager.setNotification(true);
                }
                else {
                    sessionManager.setNotification(false);
                    settingsDialog();
                }
            });
    private void settingsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog, null);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        Button buttonConfirm = dialogView.findViewById(R.id.confirm_button);
        Button buttonCancel = dialogView.findViewById(R.id.cancel_button);
        TextView text = dialogView.findViewById(R.id.textViewAlertDialog);
        text.setText("Podesite obaveštenja!");
        buttonConfirm.setText("Podesi");

        buttonConfirm.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
            dialog.dismiss();
        });
        buttonCancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void onAbout() {
        linearLayoutAbout.setEnabled(false);
        Intent intent = new Intent(getActivity(), AboutActivity.class);
        startActivity(intent);
    }
    private void onPinnedMessage() {
        linearLayoutAbout.setEnabled(false);
        Intent intent = new Intent(getActivity(), PinnedMessagesActivity.class);
        startActivity(intent);
    }
    private void onPrivacyTerms() {
        linearLayoutPrivacyTerms.setEnabled(false);
        Intent intent = new Intent(getActivity(), PrivacyTermsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        linearLayoutAbout.setEnabled(true);
        linearLayoutPrivacyTerms.setEnabled(true);
    }

}