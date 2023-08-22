package com.example.studybuddy.model;

public class PinnedMessage {
    private String id;
    private String userId;
    private String chatId;
    private String message;
    private String time;
    private String date;

    public PinnedMessage() {}

    public PinnedMessage(String id, String userId, String chatId, String message, String time, String date) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
        this.message = message;
        this.time = time;
        this.date = date;
    }

    public String getId() { return id; }

    public String getUserId() { return userId; }
    public String getChatId() { return chatId; }

    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getDate() { return date; }
}
