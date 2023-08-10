package com.example.studybuddy.model;

public class Message {
    private String id;
    private String type;
    private String sender;
    private String receiver;
    private String message;
    private MessageTime sendingTime;
    private ReplyMessage replyMessage;

    public Message() {}

    public Message(String id, String type, String sender, String receiver, String message, MessageTime sendingTime, ReplyMessage replyMessage) {
        this.id = id;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.sendingTime = sendingTime;
        this.replyMessage = replyMessage;
    }

    public String getId() { return id; }

    public String getType() { return type; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getMessage() { return message; }
    public MessageTime getSendingTime() { return sendingTime; }
    public ReplyMessage getReplyMessage() { return replyMessage; }
}
