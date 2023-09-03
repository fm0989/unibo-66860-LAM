package com.example.prjlam;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

public class BackgroundReceiver extends BroadcastReceiver {
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("broadcast receiver", "RICEVUTO!");
        int SAMPLETIME = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sTime", "15"));
        int UNTRACKEDTIME = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("untrackedAreaTime", "30"));
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {
                createAlarm(context,alarmManager,SAMPLETIME,context.getResources().getString(R.string.gather_action),Utils.REQUEST_CODE_GATHER);
                createAlarm(context,alarmManager,UNTRACKEDTIME,context.getResources().getString(R.string.untracked_action),Utils.REQUEST_CODE_UNTRACKED);
            }
            checkReportNotification(context);
        } else if(context.getResources().getString(R.string.reset_alarm_action).equals(intent.getAction())) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {return;}
            createAlarm(context,alarmManager,SAMPLETIME,context.getResources().getString(R.string.gather_action),Utils.REQUEST_CODE_GATHER);
            createAlarm(context,alarmManager,UNTRACKEDTIME,context.getResources().getString(R.string.gather_action),Utils.REQUEST_CODE_UNTRACKED);
        } else if(context.getResources().getString(R.string.gather_action).equals(intent.getAction())){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {
                Log.e("gather intent","lancio location service");
                context.startForegroundService(new Intent(context, LocationService.class));//chiama servizio foreground
                createAlarm(context, alarmManager, SAMPLETIME,context.getResources().getString(R.string.gather_action),Utils.REQUEST_CODE_GATHER);
            }
        } else if(context.getResources().getString(R.string.untracked_action).equals(intent.getAction())){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {
                Log.e("gather intent","lancio checkarea service");
                context.startForegroundService(new Intent(context, CheckAreaService.class));//chiama servizio foreground
                createAlarm(context, alarmManager, UNTRACKEDTIME,context.getResources().getString(R.string.untracked_action),Utils.REQUEST_CODE_UNTRACKED);
            }
        }
    }

    void createAlarm(Context context,AlarmManager alarm,int interval,String action,int rCode){
        if(interval<=0)
        {return;}
        Log.e("BroadcastRcv","setto allarme");
        Intent alarmintent = new Intent(context,BackgroundReceiver.class).setAction(action);
        alarmintent.putExtra("type",0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rCode, alarmintent, PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval*60000,
                pendingIntent);
    }

    void checkReportNotification(Context context){
        /* Create the Notification Channel */
        String description = context.getResources().getString(R.string.notifchreportdescr);
        NotificationChannel channel = new NotificationChannel(context.getResources().getString(R.string.notifchreportid), context.getResources().getString(R.string.notifchreport), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        //NOTIFICA REPORT SE IL GIORNO E' CORRETTO
        int t;
        try {
            t = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("daysreport", "0"));
            Log.d("broadcastrcv","daysreoirt "+t);
            if (t > 0) {
                Log.d("broadcastrcv","reporting");
                long nowT = System.currentTimeMillis();
                long reportT = PreferenceManager.getDefaultSharedPreferences(context).getLong("reportTime", 0);
                if (Long.compare(nowT,reportT)>0) {
                    Log.d("broadcastrcv","reported");
                    Intent reportIntent = new Intent(context, MainActivity.class);
                    reportIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    reportIntent.putExtra("CALLER", "notificationFromBroadcast");
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, reportIntent, PendingIntent.FLAG_IMMUTABLE);
                    Notification notification = new NotificationCompat.Builder(context, context.getResources().getString(R.string.notifchreportid))
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(context.getResources().getString(R.string.notifchreporttitle))
                            .setContentText(context.getResources().getString(R.string.notifchreporttext3))
                            .setContentIntent(pendingIntent)
                            .build();
                    notificationManager.notify(3, notification);
                }
            }
        } catch (NumberFormatException nfe) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("daysreport", "0").apply();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("reportTime", 0L).apply();
            Log.e("BroadcastRcv","daysreport not an integer");
        }
    }
}
