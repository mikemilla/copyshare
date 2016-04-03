package com.mikemilla.copyshare.data;

import java.util.List;

/**
 * Created by Mike Miller on 3/31/16.
 * Because he needed it to be created for some reason
 */
public class Contact {

    private String picture;
    private String name;
    private String number;
    private String email;
    private List<String> numbers;

    public Contact(String picture, String name, List<String> numbers, String email) {
        this.picture = picture;
        this.name = name;
        this.numbers = numbers;
        this.email = email;
    }

    public Contact(String picture, String name, String number, String email) {
        this.picture = picture;
        this.name = name;
        this.number = number;
        this.email = email;
    }

    public Contact(String name, String number, String email) {
        this.name = name;
        this.number = number;
        this.email = email;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public String getPicture() {
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

    public void setPicture(String picture) {
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
