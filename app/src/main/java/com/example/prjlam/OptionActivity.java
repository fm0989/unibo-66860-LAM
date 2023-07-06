package com.example.prjlam;

import static com.example.prjlam.Utils.BACKGROUND_PERMISSION_REQUEST_CODE;
import static com.example.prjlam.Utils.requestMyPermission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import com.example.prjlam.db.TilesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Objects;

public class OptionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Spinner spCat;
    Button btnDeletedb;
    Button btnLoaddb;
    private int selectedType=0;
    FloatingActionButton goBack;
    SharedPreferences defaultPreferences;
    private TilesViewModel mTilesViewModel;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if(key.equals("bgsampling")){
                if(sharedPreferences.getBoolean(key,false) == true) {
                    Log.d("preferences", "changed" + "  " + key + "  " + sharedPreferences.getBoolean(key, false));
                    if (!Utils.isBackgroundLocationGranted) {
                        Utils.requestMyPermission(OptionActivity.this, BACKGROUND_PERMISSION_REQUEST_CODE, false);
                        sharedPreferences.edit().putBoolean("bgsampling", false).apply();
                    } else{
                        //lancio bg receiver
                        Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
                        sendBroadcast(i);
                    }
                }
            }else if(key.equals("myreport")){
                long reportT = System.currentTimeMillis() + 86400000L * Long.parseLong(sharedPreferences.getString("myreport", "0"));
            }
        }
    };

    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.d("options", "tornato da picker");
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    String path = uri.getPath();
                    File file = new File(path);
                    Toast.makeText(getApplicationContext(),file.getName(),Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(),"SELECTED TYPE RECORDS DELETED", Toast.LENGTH_SHORT).show();
        });
        btnLoaddb = findViewById(R.id.btnLoaddb);
        btnLoaddb.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                mLauncher.launch(i);
            }catch (Exception e){
                Log.e("options","no file manager");//mettere toast
            }
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
        Utils.setGrantedPermission(requestCode, permissions, grantResults);
        if (Utils.isBackgroundLocationGranted) {
            defaultPreferences.edit().putBoolean("bgsampling",true).apply();
            //lancio bg receiver
            Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
            sendBroadcast(i);
        }
    }
}