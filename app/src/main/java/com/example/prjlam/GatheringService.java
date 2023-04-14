package com.example.prjlam;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class GatheringService extends Service {
    public GatheringService(){}
    Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    //Se muore il service???
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent foregroundIntent = new Intent(this, MainActivity.class);
            foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            foregroundIntent.putExtra("CALLER", "notificationFromForeground");
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, foregroundIntent, PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new NotificationCompat.Builder(this, "notificationServiceChannel")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("On boot notification")
                    .setContentText("This is the text")
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            //stopForeground(true);
        }
        new Thread(() -> {
            Log.d("Service","work!");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                Toast.makeText(getApplicationContext(),
                        "BACKGROUND SERVICE STARTED", Toast.LENGTH_SHORT).show();
                stopSelf();
            });
        }).start();

        return START_STICKY;
    }

}
