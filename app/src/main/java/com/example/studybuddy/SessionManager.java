package com.example.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Set;

public class SessionManager {
    SharedPreferences sharedPreferences;

    public SharedPreferences.Editor editor;
    public Context context;

    private static final String PREF_NAME = "USER";

    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String ACTIVITY_STATUS = "ACTIVITY_STATUS";

    public static final String MUTED_CHATS = "MUTED_CHATS";

    final int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createSession() {
        editor.putBoolean(NOTIFICATION, false);
        editor.putBoolean(ACTIVITY_STATUS, true);
        editor.apply();
    }

    public boolean getNotification() {
        return sharedPreferences.getBoolean(NOTIFICATION, false);
    }

    public boolean getActivityStatus() {
        return sharedPreferences.getBoolean(ACTIVITY_STATUS, false);
    }

    public void setNotification(Boolean b) {
        editor.putBoolean(NOTIFICATION, b);
        editor.apply();
    }

    public void setActivityStatus(Boolean b) {
        editor.putBoolean(ACTIVITY_STATUS, b);
        editor.apply();
    }


    public void setMutedChats(Set<String> mutedChats) {
        editor.putStringSet(MUTED_CHATS, mutedChats);
        editor.apply();
    }

    public Set<String> getMutedChats() {
        return sharedPreferences.getStringSet(MUTED_CHATS, null);
    }
}
