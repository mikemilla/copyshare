package com.mikemilla.copyshare;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button startStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStopButton = (Button) findViewById(R.id.start_stop_button);
        if (isServiceRunning(ClipboardService.class)) {
            startStopButton.setText("Stop");
        } else {
            startStopButton.setText("Start");
        }

        if (startStopButton != null) {
            startStopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isServiceRunning(ClipboardService.class)) {
                        stopService(new Intent(getBaseContext(), ClipboardService.class));
                        startStopButton.setText("Start");
                    } else {

                        // Here, thisActivity is the current activity
                        /*
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED) {

                        }
                        */

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.SEND_SMS,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.READ_CALL_LOG},
                                0);

                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_CALL_LOG)
                                == PackageManager.PERMISSION_GRANTED) {

                            Date date = new Date();
                            ArrayList<String> allnumbers = new ArrayList();
                            Cursor c = getContentResolver().query(
                                    CallLog.Calls.CONTENT_URI,
                                    null,
                                    CallLog.Calls.TYPE + " AND " + CallLog.Calls.INCOMING_TYPE
                                            + " AND " + CallLog.Calls.DATE + ">=" + date.getDate(),
                                    null, CallLog.Calls.NUMBER);

                            allnumbers.clear();
                            if (c != null)
                                c.moveToFirst();
                            for (int i = 0; c.getCount() > i; i++) {

                                String number1 = c.getString(0);

                                allnumbers.add(number1);
                                c.moveToNext();

                            }
                            searchAndDisplay(allnumbers);

                        }

                        startService(new Intent(getBaseContext(), ClipboardService.class));
                        startStopButton.setText("Stop");
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.onBackPressed();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void searchAndDisplay(ArrayList<String> arr) {

        ArrayList<String> list1 = new ArrayList();
        ArrayList<Integer> list2 = new ArrayList();
        for (int i = 0; i < arr.size(); i++) {
            int index = list1.indexOf(arr.get(i));
            if (index != -1) {
                int newCount = list2.get(index) + 1;
                list2.set(index, newCount);
            } else {
                list1.add(arr.get(i));
                list2.add(1);
            }
        }
        for (int i = 0; i < list1.size(); i++) {
            System.out.println("Number " + list1.get(i) + " occurs "
                    + list2.get(i) + " times.");

        }
        int maxCount = 0;
        int index = -1;
        for (int i = 0; i < list2.size(); i++) {
            if (maxCount < list2.get(i)) {
                maxCount = list2.get(i);
                index = i;
            }
        }
        System.out.println("Number " + arr.get(index)
                + " has highest occurrence i.e " + maxCount); // here you might want to do something/return the number with the highest occurences.
    }

}
