package com.example.studybuddy.algorithm;

import java.util.HashMap;
import java.util.Map;

public class Hashtag implements Comparable<Hashtag> {
    private Map<String, String> likes;
    private Map<String, String> comments;
    private Map<String, String> shares;

    public Hashtag() {
        this.likes = new HashMap<>();
        this.comments = new HashMap<>();
        this.shares = new HashMap<>();
    }

    public Hashtag(Map<String, String> likes, Map<String, String> comments, Map<String, String> shares) {
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
    }

    public Map<String, String> getLikes() { return likes; }
    public void setLikes(Map<String, String> likes) { this.likes = likes; }

    public Map<String, String> getComments() { return comments; }
    public void setComments(Map<String, String> comments) { this.comments = comments; }

    public Map<String, String> getShares() { return shares; }
    public void setShares(Map<String, String> shares) { this.shares = shares; }

    public int priority() {
        return likes.size() + comments.size() + shares.size();
    }

    @Override
    public int compareTo(Hashtag hashtag) {
        return this.priority() - hashtag.priority();
    }
}
