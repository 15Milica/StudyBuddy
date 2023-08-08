package com.example.studybuddy.model;

public class InviteItem {
    private String inviteName;
    private String invitePhone;

    public InviteItem(String inviteName, String invitePhoto) {
        this.inviteName = inviteName;
        this.invitePhone = invitePhoto;
    }

    public String getInviteName() {
        return inviteName;
    }

    public String getInvitePhone() {
        return invitePhone;
    }

}
