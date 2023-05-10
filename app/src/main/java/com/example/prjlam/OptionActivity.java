package com.example.prjlam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.util.Objects;

public class OptionActivity extends AppCompatActivity {

    Button btnDeletedb;
    SharedPreferences defaultPreferences;
    private TileViewModel mTileViewModel;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("preferences","changed"+"  "+key);

            if(Objects.equals(key, "maxrecorddata")){
                Log.d("maxrec", sharedPreferences.getString(key,"-1"));
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        mTileViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())
                .create(TileViewModel.class);
        btnDeletedb = findViewById(R.id.btnDeletedb);
        btnDeletedb.setOnClickListener(v -> {
            mTileViewModel.deleteAll();
        });
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        defaultPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }
}