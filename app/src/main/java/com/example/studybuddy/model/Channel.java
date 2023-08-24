package com.example.studybuddy.model;

public class Channel {
    private String channelId;
    private String channelName;
    private String groupId;

    public Channel() {}
    public Channel(String channelId, String channelName, String groupId) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.groupId = groupId;
    }
    public String getChannelId() { return channelId; }
    public String getChannelName() { return channelName; }
    public String getGroupId() { return groupId; }
}
