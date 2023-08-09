package com.example.studybuddy.model;

public class ChatItem {
    private String chatId;
    private String chatName;
    private String chatType;
    private String chatPhoto;

    public ChatItem() {
    }
    public ChatItem(String chatId, String chatName, String chatType, String chatPhoto) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.chatType = chatType;
        this.chatPhoto = chatPhoto;
    }

    public String getChatId() { return chatId; }

    public String getChatName() { return chatName; }

    public String getChatType() { return chatType; }

    public String getChatPhoto() { return chatPhoto; }
}
