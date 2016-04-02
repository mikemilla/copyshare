package com.mikemilla.copyshare.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mike Miller on 4/2/16.
 * Because he needed it to be created for some reason
 */
public class Defaults {

    public static final String PREFS = "PREFS";
    public static final String CONTACTS = "CONTACTS";

    public static void storeContacts(Context context, List<Contact> contacts) {

        // Used for store List in json format
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonContacts = gson.toJson(contacts);
        editor.putString(CONTACTS, jsonContacts);
        editor.commit();
    }

    public static List<Contact> loadContacts(Context context) {

        // Used for retrieving list from json formatted string
        SharedPreferences settings;
        List<Contact> contacts;
        settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (settings.contains(CONTACTS)) {
            String jsonContacts = settings.getString(CONTACTS, null);
            Gson gson = new Gson();
            Contact[] contactItems = gson.fromJson(jsonContacts, Contact[].class);
            contacts = Arrays.asList(contactItems);
            contacts = new ArrayList<>(contacts);
            return contacts;
        } else {
            return null;
        }
    }
}
