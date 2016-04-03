package com.mikemilla.copyshare.lists;

import java.util.List;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/05/15
 */
public class ContactModel {

    private String picture;
    private String name;
    private List<String> numbers;

    public ContactModel(String picture, String name, List<String> numbers) {
        this.picture = picture;
        this.name = name;
        this.numbers = numbers;
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

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setName(String name) {
        this.name = name;
    }

}
