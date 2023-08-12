package com.example.prjlam;

import static com.example.prjlam.Utils.BACKGROUND_PERMISSION_REQUEST_CODE;
import static com.example.prjlam.Utils.isBackgroundLocationGranted;
import static com.example.prjlam.Utils.requestMyPermission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.prjlam.db.MapRoomDatabase;
import com.example.prjlam.db.TilesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class OptionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Spinner spCat;
    Button btnDeletedb;
    Button btnLoaddb;
    Button btnSavedb;
    private int selectedType=0;
    FloatingActionButton goBack;
    SharedPreferences defaultPreferences;
    private TilesViewModel mTilesViewModel;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("bgsampling")) {
                    if (sharedPreferences.getBoolean(key, false) == true) {
                        Log.d("preferences", "changed" + "  " + key + "  " + sharedPreferences.getBoolean(key, false));
                        if (!Utils.isBackgroundLocationGranted) {
                            Utils.requestMyPermission(OptionActivity.this, BACKGROUND_PERMISSION_REQUEST_CODE, false);
                            sharedPreferences.edit().putBoolean("bgsampling", false).apply();
                        } else {
                            //lancio bg receiver
                            Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
                            sendBroadcast(i);
                        }
                    }
                } else if (key.equals("daysreport")) {
                    try {
                        if(Integer.parseInt(sharedPreferences.getString(key,"0")) > 0){
                            long reportT = System.currentTimeMillis() + 86400000L * Long.parseLong(sharedPreferences.getString("daysreport", "0"));
                            defaultPreferences.edit().putLong("reportTime", reportT).apply();
                        }else {
                            defaultPreferences.edit().putLong("reportTime", 0L).apply();
                            defaultPreferences.edit().putString("daysreport", "0").apply();
                        }
                    } catch (NumberFormatException nfe) {
                        defaultPreferences.edit().putString("daysreport", "0").apply();
                        defaultPreferences.edit().putLong("reportTime", 0L).apply();
                    }
                } else if (key.equals("untrackedAreaTime")) {
                    try {
                        if(Integer.parseInt(sharedPreferences.getString(key,"0")) <= 0){
                            defaultPreferences.edit().putString("untrackedAreaTime", "0").apply();
                        }
                    } catch (NumberFormatException nfe) {
                        defaultPreferences.edit().putString("untrackedAreaTime", "0").apply();
                    }
                }
            }
    };

    ActivityResultLauncher<Intent> saveFileLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
        new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        File dbPath = Utils.getRoomDatabasePath(getApplicationContext(),"map_database");
                        Utils.copyFile(getApplicationContext(),dbPath.getAbsolutePath(),uri);

                        Toast.makeText(getApplicationContext(), R.string.toastdone,Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    ActivityResultLauncher<Intent> loadFileLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        File dbPath = Utils.getRoomDatabasePath(getApplicationContext(),"map_database");
                        new File(dbPath.getAbsolutePath()+"-shm").delete();
                        new File(dbPath.getAbsolutePath()+"-wal").delete();
                        Utils.copyFile(getApplicationContext(),uri,dbPath.getAbsolutePath());
                        System.exit(0);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        spCat = findViewById(R.id.categoryDelete);
        spCat.setOnItemSelectedListener(this);
        mTilesViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())
                .create(TilesViewModel.class);
        btnDeletedb = findViewById(R.id.btnDeletedb);
        btnDeletedb.setOnClickListener(v -> {
            mTilesViewModel.deleteType(selectedType);
            Toast.makeText(getApplicationContext(),R.string.toastdbdeleted, Toast.LENGTH_SHORT).show();
        });
        btnLoaddb = findViewById(R.id.btnLoaddb);
        btnLoaddb.setOnClickListener(v -> {
            importDatabase();
        });
        btnSavedb = findViewById(R.id.btnSavedb);
        btnSavedb.setOnClickListener(v -> {
            exportDatabase();
        });
        goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(v -> {
            super.onBackPressed();
        });
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        defaultPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedType = Integer.parseInt(getResources().getStringArray(R.array.spinner_category_values)[i]);
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("bg PERMISSION res", "LANCIO BROADCAST");
        Utils.setGrantedPermission(requestCode, permissions, grantResults);
        if (Utils.isBackgroundLocationGranted) {
            defaultPreferences.edit().putBoolean("bgsampling",true).apply();
            //lancio bg receiver
            Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
            sendBroadcast(i);
        }
    }

    public void exportDatabase() {
        mTilesViewModel.checkpointDatabase();
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE, "map_database");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        saveFileLauncher.launch(i);
    }
    public void importDatabase() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("application/octet-stream");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        loadFileLauncher.launch(i);
    }
}