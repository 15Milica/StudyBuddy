package com.example.studybuddy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Like {

    public String id;
    public Map<String, String> users;
    public boolean visibility;
    public Like() {
        users = new HashMap<>();
    }

    public Like(String id, Map<String, String> users, boolean visibility) {
        this.id = id;
        this.users = users;
        this.visibility = visibility;
    }

    public String getId() { return id; }

    public Map<String, String> getUsers() {
        return users;
    }

    public boolean isVisibility() { return visibility; }
}
