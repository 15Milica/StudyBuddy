package com.example.studybuddy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.EditText;

import java.util.Calendar;

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

}
