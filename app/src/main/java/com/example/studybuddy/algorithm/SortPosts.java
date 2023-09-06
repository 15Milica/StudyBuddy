package com.example.studybuddy.algorithm;

import com.example.studybuddy.model.Post;

import java.util.Comparator;
import java.util.Map;

public class SortPosts implements Comparator<Post> {
    private Map<String, Hashtag> hashtags;

    public SortPosts(Map<String, Hashtag> hashtags) {
        this.hashtags = hashtags;
    }

    private int priority(Post post) {
        int p = 0;

        for(String hash : post.getHashtags()) {
            Hashtag hashtag = hashtags.get(hash);
            if(hashtag != null) p += hashtag.priority();
        }

        return p;
    }

    public int compare(Post post1, Post post2) {
        return priority(post2) - priority(post1);
    }
}
