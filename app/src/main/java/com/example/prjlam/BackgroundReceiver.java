package com.example.prjlam;

import android.Manifest;
import android.app.AlarmManager;
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
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.util.Calendar;
import java.util.Date;

public class BackgroundReceiver extends BroadcastReceiver {
    private int SAMPLETIME;
    private AlarmManager alarmManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        /* Create the Notification Channel */
        CharSequence name = "newDataReportChannel";
        String description = "Notification of the periodic report on the analysis of the new areas";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


        Log.e("broadcast receiver", "RICEVUTO!");
        SAMPLETIME = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("sTime", String.valueOf(SAMPLETIME)));
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || context.getResources().getString(R.string.reset_alarm_action).equals(intent.getAction())) {
            //NOTIFICA REPORT SE IL GIORNO COINCIDE
            long nowT = System.currentTimeMillis();
            long reportT = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString("myreport", "0"));
            if(nowT > reportT){
                //notifia
            }
            Log.e("BackRcv","setto allarme");
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {return;}
            createAlarm(context,alarmManager,SAMPLETIME);
        } else if(context.getResources().getString(R.string.gather_action).equals(intent.getAction())){
            Log.e("gather intent","lancio gather service");
            context.startForegroundService(new Intent(context, LocationService.class));//chiama servizio foreground
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                PreferenceManager.getDefaultSharedPreferences(context).getBoolean("bgsampling",false)) {
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
