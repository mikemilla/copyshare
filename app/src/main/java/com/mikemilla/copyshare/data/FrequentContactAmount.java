package com.mikemilla.copyshare.data;

import android.support.annotation.NonNull;

/**
 * Created by Mike Miller on 3/31/16.
 * Because he needed it to be created for some reason
 */
public class FrequentContactAmount implements Comparable<FrequentContactAmount> {

    private String number;
    private Integer amount;

    public FrequentContactAmount(String number, Integer amount) {
        this.number = number;
        this.amount = amount;
    }

    public String getNumber() {
        return number;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public int compareTo(@NonNull FrequentContactAmount another) {
        return this.getAmount() - another.getAmount();
    }
}
