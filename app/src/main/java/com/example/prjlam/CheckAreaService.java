package com.example.prjlam;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.prjlam.db.MapTile;
import com.example.prjlam.db.TilesViewModel;
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

import java.security.Provider;
import java.util.List;

public class CheckAreaService extends Service {
    public CheckAreaService() {
    }

    Handler handler;
    private boolean mRunning = false;

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private SettingsClient client;
    private TilesViewModel mTilesViewModel;
    private Observer observer = new Observer<List<MapTile>>() {
        @Override
        public void onChanged(List<MapTile> mapTiles) {
            Log.e("checkareaservice", "tracked check"+mapTiles.size());
            if(mapTiles.size()==0) {
                Intent untrackedIntent = new Intent(getApplicationContext(), MainActivity.class);
                untrackedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                untrackedIntent.putExtra("CALLER", "notificationFromCheckAreaService");
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Utils.REQUEST_CODE_UNTRACKED, untrackedIntent, PendingIntent.FLAG_IMMUTABLE);
                Notification notification = new NotificationCompat.Builder(getApplicationContext(), getResources().getString(R.string.notifichuntrackedid))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(getResources().getString(R.string.notifichuntrackedtitle))
                        .setContentText(getResources().getString(R.string.notifichuntrackedtext))
                        .setContentIntent(pendingIntent)
                        .build();
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.notify(4, notification);
            }
            stopSelf();
        }
    };



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
        mTilesViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(TilesViewModel.class);
        mTilesViewModel.getChekedTile().observeForever(observer);
        NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.notifichuntrackedid), getResources().getString(R.string.notifichuntracked), NotificationManager.IMPORTANCE_NONE);
        channel.setDescription(getResources().getString(R.string.notifichuntrackeddescr));
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    Log.e("locationresult", "VUOTO!");
                    stopSelf();
                }
                //query al db
                mTilesViewModel.checkTileEntry(Utils.customSizeTile(locationResult.getLastLocation().getLatitude(),true), Utils.customSizeTile(locationResult.getLastLocation().getLongitude(),false));
                fusedLocationClient.removeLocationUpdates(locationCallback);
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
        Notification notification = new NotificationCompat.Builder(this, getResources().getString(R.string.notifichuntrackedid))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getResources().getString(R.string.notifchbgtitle))
                .setContentText(getResources().getString(R.string.notifchbgtext2))
                .setContentIntent(pendingIntent)
                .build();
        startForeground(3, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("locationservice", "START!");
        if (!this.mRunning) {
            this.mRunning = true;

            new Thread(() -> {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    stopSelf();
                }
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            Log.e("location onsuccesslistener", "NO PERMESSI!");
                            stopSelf();
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                locationCallback,
                                Looper.getMainLooper());
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopSelf();
                    }
                });

            }).start();
        }else{
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("checkareaservice", "destroyed");
        mTilesViewModel.getChekedTile().removeObserver(observer);
        this.mRunning = false;
    }
}
