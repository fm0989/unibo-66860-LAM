package com.example.prjlam;

import static com.example.prjlam.Utils.BACKGROUND_PERMISSION_REQUEST_CODE;
import static com.example.prjlam.Utils.requestLocationPermissions;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import android.widget.Button;
import android.widget.Toast;

import com.example.prjlam.db.TilesViewModel;

import java.io.File;
import java.util.Objects;

public class OptionActivity extends AppCompatActivity {

    Button btnDeletedb;
    Button btnLoaddb;
    SharedPreferences defaultPreferences;
    private TilesViewModel mTilesViewModel;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("preferences","changed"+"  "+key);

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
        mTilesViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())
                .create(TilesViewModel.class);
        btnDeletedb = findViewById(R.id.btnDeletedb);
        btnDeletedb.setOnClickListener(v -> {
            mTilesViewModel.deleteAll();
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
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        defaultPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }
}