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
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.prjlam.db.MapTile;
import com.example.prjlam.db.TilesViewModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GatheringService extends Service {
    public GatheringService() {
    }

    Handler handler;
    private boolean mRunning = false;
    private TilesViewModel mTilesViewModel;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        mTilesViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(TilesViewModel.class);

        NotificationChannel channel = new NotificationChannel(BACKGROUND_NOTIFICATION_NAME.toString(), BACKGROUND_NOTIFICATION_NAME, NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("Foreground notification when gathering data in background");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.mRunning) {
            this.mRunning = true;
            Intent foregroundIntent = new Intent(this, MainActivity.class);
            foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            foregroundIntent.putExtra("CALLER", "notificationFromForeground");
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, foregroundIntent, PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new NotificationCompat.Builder(this, (String) BACKGROUND_NOTIFICATION_NAME)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Background data gathering")
                    .setContentText("Service is gathering Wifi,Lte and noise data")
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(2, notification);

            Location location = intent.getParcelableExtra("location");
            if(location==null){
                Log.e("gatherservice","NO LOCATION");
                this.mRunning = false;
                stopSelf();
            }

            new Thread(() -> {
                //DATA GATHERING
                Map<Integer, Integer> dataGathered = new HashMap<Integer, Integer>();//type 0-2 ,level 0-100
                int result;
                result = gatherLte();
                if(result!=-1){dataGathered.put(0,result);}
                result = gatherWifi();
                if(result!=-1){dataGathered.put(1,result);}
                result = gatherNoise();
                if(result!=-1){dataGathered.put(2,result);}
                Log.e("gatherservice", "FATTO TUTTO!");
                //query db
                dataGathered.forEach((type,v) -> {
                    mTilesViewModel.insertTile(new MapTile(customSizeTile(location.getLatitude(), true),
                                                            customSizeTile(location.getLongitude(), false),
                                                            v,
                                                            type,
                                                            System.currentTimeMillis()));
                    Log.d("inserted data", "type "+String.valueOf(type)+" value "+String.valueOf(v));
                });
                mRunning = false;
                stopSelf();
                handler.post(() -> {//Cose da fare in UI
                    //Toast.makeText(getApplicationContext(),"BACKGROUND SERVICE ENDED", Toast.LENGTH_SHORT).show();
                });
            }).start();
;
        }
        return START_NOT_STICKY;

    }
    private int gatherLte(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            for (CellInfo cellInfo : cellInfos) {
                if (cellInfo instanceof CellInfoLte) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Log.e("gather from this", String.valueOf(cellInfo.getCellSignalStrength().getLevel()));
                        return cellInfo.getCellSignalStrength().getLevel() * 25;
                    }
                    Log.e("gather from this", String.valueOf(telephonyManager.getSignalStrength().getLevel()));
                    return telephonyManager.getSignalStrength().getLevel() * 25;
                }
            }
        }
        return -1;
    }
    private int gatherWifi(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED) {
                String name = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("myssid", "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (name.equals("") || wifiManager.getConnectionInfo().getSSID().substring(1, wifiManager.getConnectionInfo().getSSID().length() - 1).equals(name)) {
                        return wifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi()) * 25;
                    }
                }
                else if(name.equals("") || wifiManager.getConnectionInfo().getSSID().substring(1, wifiManager.getConnectionInfo().getSSID().length() - 1).equals(name)){
                    return wifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4) * 25 + 25;
                }
            }
        }
        return -1;
    }
    private int gatherNoise(){
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.e("gatherservice", "MEDIA RECORDO!");
            MediaRecorder mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(getExternalCacheDir()+"/ampltest.3gp");
            Timer timer = new Timer();
            RecorderTask rt = new RecorderTask(mediaRecorder);
            timer.scheduleAtFixedRate(rt, 0, 500);
            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException | IOException e) {
                Log.e("mediarecorder","prepare failed");
            }
            int maxamp = -1;
            try {
                mediaRecorder.start();
                Thread.sleep(5000);
                mediaRecorder.stop();
                timer.cancel();
                maxamp = rt.getAmplitudeDb();
            } catch (IllegalStateException | InterruptedException e){
                e.printStackTrace();
            }
            mediaRecorder.release();
            return maxamp-10;//
        }
        return -1;
    }
    private class RecorderTask extends TimerTask {
        private MediaRecorder recorder;
        private int amplitudeDb;
        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }
        public void run() {
            try {
                int amplitude = recorder.getMaxAmplitude()+1;
                amplitudeDb = (int) (20 * Math.log10((double) Math.abs(amplitude)));
            } catch (IllegalStateException e){
                Log.e("Recorder Timer","maxamplitude crash");
            }
        }
        public int getAmplitudeDb(){return amplitudeDb;}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("gatherservice", "destroyed");
        this.mRunning = false;
    }
}
