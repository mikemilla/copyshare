package com.mikemilla.copyshare.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClipboardReceiver extends BroadcastReceiver {

    public ClipboardReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ClipboardService.class));
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
