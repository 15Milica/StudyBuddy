package com.example.studybuddy.model;

public class Comment {
    public String id;
    public String user;
    public String text;

    public Comment() {}
    public Comment(String id, String user, String text) {
        this.id = id;
        this.user = user;
        this.text = text;
    }

    public String getId() { return id; }

    public String getUser() { return user; }

    public String getText() { return text; }
}
