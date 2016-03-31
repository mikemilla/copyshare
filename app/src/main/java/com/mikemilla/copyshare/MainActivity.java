package com.mikemilla.copyshare;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Init
    private Animation slideUp, fadeIn;
    private CoordinatorLayout mCoordinatorLayout;
    private View background;
    private BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;
    private FrameLayout mBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the animations
        createAnimations();

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        background = findViewById(R.id.background);
        mBottomSheet = (FrameLayout) mCoordinatorLayout.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        // Closes sheet on background click
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBottomSheetBehavior.setPeekHeight(mBottomSheet.getMeasuredHeight());
            }
        });

        // Listens to bottom sheet changes
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // Finishes activity when hidden
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                // Animates the background on slide
                if (background != null) {
                    background.setAlpha(1 + slideOffset);
                }
            }
        });

        // Animate first load
        mBottomSheet.setAnimation(slideUp);
        background.setAnimation(fadeIn);

        // Recycler List
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerAdapter adapter = new RecyclerAdapter();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }

        // Text View at top
        TextView mActionButton = (TextView) findViewById(R.id.action_button);
        if (mActionButton != null) {
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });
        }

        // Switch
        Switch mServiceSwitch = (Switch) findViewById(R.id.service_switch);
        if (mServiceSwitch != null) {

            // Starts the service by default
            mServiceSwitch.setChecked(true);
            startService();

            // Listen to check changes
            mServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        startService();
                    } else {
                        if (isServiceRunning(ClipboardService.class)) {
                            stopService(new Intent(getBaseContext(), ClipboardService.class));
                        }
                    }
                }
            });
        }

    }

    private void createAnimations() {
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    }

    private void startService() {
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

    @Override
    public void onBackPressed() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //mBackgroundView.startAnimation(fadeOut);
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
