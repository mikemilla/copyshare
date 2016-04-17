package com.mikemilla.copyshare.data;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Mike Miller on 4/16/16.
 * Because he needed it to be created for some reason
 */
public class CopyShareApplication extends Application {

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-51859037-5");
            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }

}
