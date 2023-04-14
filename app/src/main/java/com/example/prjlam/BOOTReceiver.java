package com.example.prjlam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BOOTReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("Broadcast", "work!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, GatheringService.class));//chiama servizio foreground
            } else {
                context.startService(new Intent(context, GatheringService.class));//chiama servizio
            }
        }
    }
}
