package com.example.prjlam;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public abstract class Utils {
    public final static  double NTILES = 100000;
    public final static double halfWidth = 360 / NTILES / 3;//360 LONG
    public final static double halfHeight = 180 / NTILES / 2;//180 LAT

    public static final CharSequence BACKGROUND_NOTIFICATION_NAME = "BackgroundServiceChannel";

    public static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int NETWORK_PERMISSION_REQUEST_CODE = 2;
    public static final int WIFI_PERMISSION_REQUEST_CODE = 3;
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 4;
    public static final int BACKGROUND_PERMISSION_REQUEST_CODE = 5;
    /**
     * Requests the fine and coarse location permissions.
     */
    public static void requestLocationPermissions(AppCompatActivity activity, int requestCode,
                                                  boolean finishActivity) {
        if(requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    requestCode);
        }
        if(requestCode == NETWORK_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    requestCode);
        }
        if(requestCode == WIFI_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    requestCode);
        }
        if(requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    requestCode);
        }
        if(requestCode == BACKGROUND_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    requestCode);
        }
    }

    /**
     * Checks if the result contains a PERMISSION_GRANTED result for a
     * permission from a runtime permissions request.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public static double customSizeTile(double latlon, boolean islat) {
        if(islat){
            return truncate(Math.round((latlon + 90 - halfHeight) / 2 / halfHeight) * 2 * halfHeight - 90 + halfHeight);
        }
        return truncate(Math.round((latlon + 180 - halfWidth) / 2 / halfWidth) * 2 * halfWidth - 180 + halfWidth);
    }
    public static double truncate(double value)
    {
        value = value * Math.pow(10, 4);
        value = Math.round(value);
        value = value / Math.pow(10, 4);
        return value;
    }

}
