package com.example.studybuddy.model;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private String groupId;
    private String groupName;
    private String groupAdmin;
    private String groupPhoto;
    private List<String> members;
    private List<String> tokens;
    private String search;

    public Group(){}

    public Group(String groupId, String groupName, String groupAdmin, String groupPhoto, List<String> members, List<String> tokens, String search) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupAdmin = groupAdmin;
        this.groupPhoto = groupPhoto;
        this.members = members;
        this.tokens = tokens;
        this.search = search;
    }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getGroupAdmin() { return groupAdmin; }
    public String getGroupPhoto() {return groupPhoto; }
    public List<String> getMembers() { return members; }
    public List<String> getTokens() { return tokens; }

    public String getSearch() { return search; }
}
