package com.example.prjlam;

import static com.example.prjlam.Utils.REPORT_NOTIFICATION_NAME;

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
        /* Create the Notification Channel */
        String description = "Notification of the periodic report on the analysis of the new areas";
        NotificationChannel channel = new NotificationChannel(REPORT_NOTIFICATION_NAME.toString(), REPORT_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


        Log.e("broadcast receiver", "RICEVUTO!");
        int SAMPLETIME = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sTime", "15"));
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.e("BroadcastRcv","setto allarme");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {return;}
            createAlarm(context,alarmManager,SAMPLETIME);
            //NOTIFICA REPORT SE IL GIORNO E' CORRETTO
            int t = 0;
            try {
                t = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("daysreport", "0"));
            } catch (NumberFormatException nfe) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("daysreport", "0").apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("reportTime", 0L).apply();
            }
            if (t > 0) {
                long nowT = System.currentTimeMillis();
                long reportT = PreferenceManager.getDefaultSharedPreferences(context).getLong("reportTime", 0);
                if (nowT > reportT) {
                    Intent reportIntent = new Intent(context, BackgroundReceiver.class);
                    reportIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    reportIntent.putExtra("CALLER", "notificationFromBroadcast");
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, reportIntent, PendingIntent.FLAG_IMMUTABLE);
                    Notification notification = new NotificationCompat.Builder(context, (String) REPORT_NOTIFICATION_NAME)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Report of data collected from new areas")
                            .setContentText("Open the app to view the results")
                            .setContentIntent(pendingIntent)
                            .build();
                    notificationManager.notify(3, notification);
                }
            }
        } else if(context.getResources().getString(R.string.reset_alarm_action).equals(intent.getAction())) {
            Log.e("BroadcastRcv","setto allarme");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {return;}
            createAlarm(context,alarmManager,SAMPLETIME);
        } else if(context.getResources().getString(R.string.gather_action).equals(intent.getAction())){
            Log.e("gather intent","lancio gather service");
            context.startForegroundService(new Intent(context, LocationService.class));//chiama servizio foreground
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {
                Log.e("BroadcastRcv","setto allarme");
                createAlarm(context, alarmManager, SAMPLETIME);
            }
        }
    }

    void createAlarm(Context context,AlarmManager alarm,int interval){
        Intent alarmintent = new Intent(context,BackgroundReceiver.class).setAction(context.getResources().getString(R.string.gather_action));
        alarmintent.putExtra("type",0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmintent, 0);
        alarm.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval*60000,
                pendingIntent);
    }
}
