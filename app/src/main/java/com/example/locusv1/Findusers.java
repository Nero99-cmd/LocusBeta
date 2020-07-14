package com.example.locusv1;

public class Findusers {
    String UserName, Name;

    public Findusers() {
    }

    public Findusers(String userName, String name) {
        UserName = userName;
        Name = name;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

}
