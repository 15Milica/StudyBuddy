package com.example.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Check {

    public static boolean validFirstName(String first_name){
        if(first_name.length() == 0) return false;
        return first_name.matches("^[A-Z][a-z]+$");
    }

    public static boolean validLastName(String last_name){
        if(last_name.length() == 0) return false;
        return last_name.matches("^[A-Z][a-z]+$");
    }
    public static boolean validDate(String date){
        if(date.isEmpty()) return false;

        String[] birthday = date.split("/");

        int year = Integer.parseInt(birthday[2]);
        int current_year = Calendar.getInstance().get(Calendar.YEAR);

        if(year >= current_year) return false;
        if(year <= 1900) return false;

        return true;
    }

    public static boolean networkConnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static void enable (EditText first_name, EditText last_name, EditText birthday, EditText description, CircleImageView image, boolean b)
    {
        first_name.setEnabled(b);
        last_name.setEnabled(b);
        birthday.setEnabled(b);
        description.setEnabled(b);
        image.setEnabled(b);
    }
    public static boolean isFollower(String followerId, List<String> listFollowers){ return  listFollowers.contains(followerId); }
    public static void enableButtonPagePost(Button like, Button comment, Button share, TextView description, boolean b) {
        like.setEnabled(b);
        comment.setEnabled(b);
        share.setEnabled(b);
        description.setEnabled(b);
    }
    public static void settingsPagePost(TextView description, LinearLayout hide, CoordinatorLayout coordinatorLayoutFullDescription, CoordinatorLayout coordinatorLayoutSettings, ConstraintLayout constraintLayoutComments, ImageButton option){
        description.setVisibility(View.VISIBLE);
        hide.setVisibility(View.GONE);
        constraintLayoutComments.setVisibility(View.GONE);
        coordinatorLayoutSettings.setVisibility(View.GONE);
        coordinatorLayoutFullDescription.setVisibility(View.GONE);
        option.setActivated(false);
    }
    public static boolean isEmpty(EditText editText){ return editText.getText().toString().trim().length() == 0; }
}
