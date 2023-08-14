package com.example.studybuddy.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    public String id;
    public String user;
    public String type;
    public String post;
    public String location;
    public List<String> hashtags;
    public String description;
    public boolean optionShare;
    public boolean optionComments;

    public static final String POST_TYPE_TEXT = "text";
    public static final String POST_TYPE_VIDEO = "video";
    public static final String POST_TYPE_IMAGE = "image";

    public Post() { hashtags = new ArrayList<>(); }

    public Post(String id, String user, String type, String post, String location, List<String> hashtags, String description, boolean optionShare, boolean optionComments) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.post = post;
        this.location = location;
        this.hashtags = hashtags;
        this.description = description;
        this.optionShare = optionShare;
        this.optionComments = optionComments;
    }

    public String getId() { return id; }
    public String getUser() { return user; }

    public String getType() { return type; }
    public String getPost() { return post; }
    public String getLocation() { return location; }
    public List<String> getHashtags() { return hashtags; }
    public String getDescription() { return description; }
    public boolean isOptionShare() { return optionShare; }
    public boolean isOptionComments() { return optionComments; }
}
