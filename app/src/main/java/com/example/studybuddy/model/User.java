package com.example.studybuddy.model;

import java.io.Serializable;

public class User implements Serializable {

    private String userId;
    private String firstName;
    private String lastName;
    private String description;
    private String birthday;
    private String photo;
    private String numberPhone;
    private String search;
    private String token;

    public User() { }

    public User(String userId, String firstName, String lastName, String birthday, String description, String photo, String numberPhone, String search, String token){
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.description = description;
        this.photo = photo;
        this.numberPhone = numberPhone;
        this.search = search;
        this.token = token;
    }

    public User(String userId, String firstName, String lastName, String birthday, String description, String numberPhone, String search) {
        this(userId, firstName, lastName, birthday, description, "default", numberPhone, search,"");
    }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getBirthday() { return birthday; }
    public String getDescription() { return description; }
    public String getNumberPhone() { return numberPhone;}
    public String getPhoto() { return photo; }
    public String getSearch() { return search; }
    public String getToken() { return token; }
}
