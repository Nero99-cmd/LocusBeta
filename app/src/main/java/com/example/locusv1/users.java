package com.example.locusv1;

import android.content.Intent;

public class users {
    public String Name, UserName, Email;
    public double Lat, Lang;
    public int Status;

    public users() {
    }

    public users(String name, String userName, String email, double lat, double lang, int status) {
        Name = name;
        UserName = userName;
        Email = email;
        Lat = lat;
        Lang = lang;
        Status = status;
    }
}
