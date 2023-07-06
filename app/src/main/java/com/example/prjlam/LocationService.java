package com.example.prjlam;

import static com.example.prjlam.Utils.BACKGROUND_NOTIFICATION_NAME;
import static com.example.prjlam.Utils.customSizeTile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.prjlam.db.MapTile;
import com.example.prjlam.db.TilesViewModel;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationService extends Service {
    public LocationService() {
    }

    Handler handler;
    private boolean mRunning = false;

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private SettingsClient client;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Log.e("locationservice", "NATO!");
        NotificationChannel channel = new NotificationChannel(BACKGROUND_NOTIFICATION_NAME.toString(), BACKGROUND_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_NONE);
        channel.setDescription("Foreground notification when gathering data in background");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    Log.e("locationresult", "VUOTO!");
                    stopSelf();
                }
                Log.e("LOCAT SERVICE","loc " + locationResult.getLastLocation().toString());
                Log.e("LOCAT SERVICE","CHIAMATO GATHER");
                getApplicationContext().startForegroundService(new Intent(getApplicationContext(), GatheringService.class).putExtra("location", locationResult.getLastLocation()));
                fusedLocationClient.removeLocationUpdates(locationCallback);
                stopSelf();
            }
        };
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        client = LocationServices.getSettingsClient(this);

        Intent foregroundIntent = new Intent(this, MainActivity.class);
        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        foregroundIntent.putExtra("CALLER", "notificationFromForeground");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, foregroundIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, (String) BACKGROUND_NOTIFICATION_NAME)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Background data gathering")
                .setContentText("Service is collecting location coordinates")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("locationservice", "START!");
        if (!this.mRunning) {
            this.mRunning = true;

            new Thread(() -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    stopSelf();
                }
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            Log.e("location onsuccesslistener", "NO PERMESSI!");
                            stopSelf();
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback,
                                Looper.getMainLooper());
                        Log.e("locationservice", "LOC REQUESTED!");
                    }
                });
            }).start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("locationservice", "destroyed");
        this.mRunning = false;
    }
}
