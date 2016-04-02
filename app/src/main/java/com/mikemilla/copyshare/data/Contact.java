package com.mikemilla.copyshare.data;

import android.graphics.Bitmap;

/**
 * Created by Mike Miller on 3/31/16.
 * Because he needed it to be created for some reason
 */
public class Contact {

    private Bitmap picture;
    private String name;
    private String number;
    private String email;

    public Contact(String name, String number, String email) {
        this.name = name;
        this.number = number;
        this.email = email;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
