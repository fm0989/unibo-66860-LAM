package com.example.prjlam;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    ImageButton btnZin,btnZout,btnSett;
    Spinner spCat;
    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
    new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK)
            {Intent data = result.getData();}
            //
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnZin = findViewById(R.id.zoomIn);
        btnZout = findViewById(R.id.zoomOut);
        btnSett = findViewById(R.id.btnSettings);
        btnSett.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), OptionActivity.class);
            mLauncher.launch(i);
        });
        /* Spinner */
        spCat = findViewById(R.id.category);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_category, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spCat.setAdapter(spinnerAdapter);
    }
}