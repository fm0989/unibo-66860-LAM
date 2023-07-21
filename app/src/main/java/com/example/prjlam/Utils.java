package com.example.prjlam;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import kotlin.jvm.Throws;

public abstract class Utils {
    public final static  double NTILES = 100000;
    public final static double halfWidth = 360 / NTILES / 3;//360 LONG
    public final static double halfHeight = 180 / NTILES / 2;//180 LAT

    public static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final int AUDIO_PERMISSION_REQUEST_CODE = 2;
    public static final int BACKGROUND_PERMISSION_REQUEST_CODE = 3;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 4;

    public static boolean isLocationGranted = false;
    public static boolean isBackgroundLocationGranted = false;
    public static boolean isAudioGranted = false;
    public static boolean isNotificationGranted = false;

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
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            isNotificationGranted = true;
        } else {
            isNotificationGranted = false;
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
        if(requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
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
        if(requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (isPermissionGranted(permissions, grantResults, Manifest.permission.POST_NOTIFICATIONS)){
                isNotificationGranted = true;
            } else {
                isNotificationGranted = false;
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
    public static File getRoomDatabasePath(Context context, String dbName) throws Exception {
        String dbFolderPath = context.getFilesDir().getAbsolutePath().replace("files", "databases");
        File dbFile = new File(dbFolderPath+'/'+dbName);

        // Check if database file exist.
        if (!dbFile.exists()) throw new Exception(dbFile.getAbsolutePath() + "doesn't exist");

        return dbFile;
    }
    public static void copyFile(Context context,String inputPath,Uri outputPath) throws IOException {
        Log.e("Utils",inputPath+"  "+outputPath+" "+outputPath.getAuthority());
        FileInputStream fin = new FileInputStream(inputPath);

        Files.copy(Paths.get(inputPath),context.getContentResolver().openOutputStream(outputPath,"w"));
        fin.close();
    }
    public static void copyFile(Context context,Uri inputPath,String outputPath) throws IOException {
        Log.e("Utils",inputPath+"  "+outputPath+" "+inputPath.getAuthority());
        FileOutputStream fout = new FileOutputStream(outputPath);

        Files.copy(context.getContentResolver().openInputStream(inputPath),Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
        fout.close();
    }
}
