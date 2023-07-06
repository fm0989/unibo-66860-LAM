package com.example.prjlam;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    public static boolean isLocationGranted = false;
    public static boolean isBackgroundLocationGranted = false;

    public static boolean isNetworkGranted = false;
    public static boolean isWifiGranted = false;
    public static boolean isAudioGranted = false;

    /**
     * Set initial local permission vars
     */
    public static void checkPermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                isLocationGranted = true;
        } else {
                isLocationGranted = false;
            }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
            == PackageManager.PERMISSION_GRANTED) {
                isNetworkGranted = true;
        } else {
                isNetworkGranted = false;
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
            == PackageManager.PERMISSION_GRANTED) {
            isWifiGranted = true;
        } else {
            isWifiGranted = false;
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            isAudioGranted = true;
        } else {
            isAudioGranted = false;
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            isBackgroundLocationGranted = true;
        } else {
            isBackgroundLocationGranted = false;
        }
    }
    /**
     * Requests the fine and coarse location permissions.
     */
    public static void requestMyPermission(AppCompatActivity activity, int requestCode,
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
     *  Check if a permission request is satisfied and update local permissions vars
     */

    public static void setGrantedPermission(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION) ||
                isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                isLocationGranted = true;
            } else {
                isLocationGranted = false;
            }
        }
        if(requestCode == NETWORK_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_NETWORK_STATE)){
                isNetworkGranted = true;
            } else {
                isNetworkGranted = false;
            }
        }
        if(requestCode == WIFI_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_WIFI_STATE)){
                isWifiGranted = true;
            } else {
                isWifiGranted = false;
            }
        }
        if(requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.RECORD_AUDIO)){
                isAudioGranted = true;
            } else {
                isAudioGranted = false;
            }
        }
        if(requestCode == BACKGROUND_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                isBackgroundLocationGranted = true;
            } else {
                isBackgroundLocationGranted = false;
            }
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

    public static void checkGPS(Context context) {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gps_enabled) {
            isLocationGranted = false;
            // notify user
            new AlertDialog
            .Builder(context)
            .setMessage(R.string.no_gps_network)
            .setPositiveButton(R.string.open_loc_sett, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.dismiss();
                    context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            })
            .setNegativeButton(R.string.cancel,null)
            .show();
        }
    }
}
