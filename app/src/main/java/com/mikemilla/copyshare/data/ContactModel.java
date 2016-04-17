package com.mikemilla.copyshare.data;

import java.util.List;
import java.util.Random;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/05/15
 */
public class ContactModel {

    private String picture;
    private String name;
    private List<String> numbers;
    private String number;
    private int drawable;
    private boolean selected = false;

    public ContactModel(String picture, String name, List<String> numbers, String number) {
        this.picture = picture;
        this.name = name;
        this.numbers = numbers;
        this.number = number;
        this.drawable = setRandomInt();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String numbers) {
        this.number = number;
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

    private int setRandomInt() {
        int min = 0;
        int max = 6 - 1;
        Random r = new Random();
        return r.nextInt(max - min + 1) + min;
    }

    public int getColor() {
        return drawable;
    }

    public String getNameLetter() {
        return String.valueOf(name.charAt(0));
    }

}
