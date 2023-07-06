package com.example.prjlam;

import static com.example.prjlam.Utils.BACKGROUND_PERMISSION_REQUEST_CODE;
import static com.example.prjlam.Utils.MY_LOCATION_PERMISSION_REQUEST_CODE;
import static com.example.prjlam.Utils.customSizeTile;
import static com.example.prjlam.Utils.halfHeight;
import static com.example.prjlam.Utils.halfWidth;
import static com.example.prjlam.Utils.isAudioGranted;
import static com.example.prjlam.Utils.isBackgroundLocationGranted;
import static com.example.prjlam.Utils.isLocationGranted;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prjlam.db.MapTile;
import com.example.prjlam.db.TilesViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    ImageButton btnSett;
    Button btnSX, btnMD, btnDX, btnGather;
    TextView areaStatus;
    Spinner spCat;
    SharedPreferences defaultPreferences;
    private TilesViewModel mTilesViewModel;
    /* MAPS SETTS */
    private GoogleMap map;
    private int mapType;
    private int maxTileData = 4;
    private static final int NODATA = Color.argb(60, 170, 170, 170);
    private static boolean[] cENABLE = {true, true, true};
    private static int LOW;
    private static int AVERAGE;
    private static int HIGH;
    private static int LOWT;
    private static int AVERAGET;
    private static int HIGHT;

    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("main", "tornato da opzioni");
                    preferenceInit();
                    if (result.getResultCode() == Activity.RESULT_OK) {

                    }
                    //
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Create the Notification Channel */
        CharSequence name = "notificationServiceChannel";
        String description = "Whatever";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        setContentView(R.layout.activity_main);
        btnSX = findViewById(R.id.btnSX);
        btnMD = findViewById(R.id.btnMD);
        btnDX = findViewById(R.id.btnDX);
        btnGather = findViewById(R.id.btnGather);
        areaStatus = findViewById(R.id.dataStatus);
        spCat = findViewById(R.id.category);
        spCat.setOnItemSelectedListener(this);
        /* SET DEFAULT CATEGORY */
        AVERAGE = getApplicationContext().getResources().getColor(R.color.medium, null);
        AVERAGET = getApplicationContext().getResources().getColor(R.color.mediumT, null);
        changeMapType(0);
        /* SET PREFERENCES */
        Utils.checkPermissions(this);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferenceInit();

        btnSett = findViewById(R.id.btnSettings);
        btnSett.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), OptionActivity.class);
            mLauncher.launch(i);
        });
        btnSX.setOnClickListener(v -> {
            cENABLE[0] = !cENABLE[0];
            if (cENABLE[0]) {
                btnSX.setBackgroundColor(LOW);
            } else {
                btnSX.setBackgroundColor(Color.rgb(192, 192, 192));
            }
            queryMapData();
        });
        btnMD.setOnClickListener(v -> {
            cENABLE[1] = !cENABLE[1];
            if (cENABLE[1]) {
                btnMD.setBackgroundColor(AVERAGE);
            } else {
                btnMD.setBackgroundColor(Color.rgb(192, 192, 192));
            }
            queryMapData();
        });
        btnDX.setOnClickListener(v -> {
            cENABLE[2] = !cENABLE[2];
            if (cENABLE[2]) {
                btnDX.setBackgroundColor(HIGH);
            } else {
                btnDX.setBackgroundColor(Color.rgb(192, 192, 192));
            }
            queryMapData();
        });

        /* Start gathering info's */
        btnGather.setOnClickListener(v -> {
            // check permessi
            Utils.checkPermissions(this);
            if (!Utils.isLocationGranted) {
                Toast.makeText(this, "THIS DONT HAPPEN", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!isAudioGranted) {
                // notify user
                new AlertDialog
                        .Builder(this)
                        .setMessage(R.string.no_audio_permission)
                        .setPositiveButton(R.string.open_app_sett, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                paramDialogInterface.dismiss();
                                MainActivity.this.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + MainActivity.this.getPackageName())));
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                btnGather.setText(MainActivity.this.getResources().getString(R.string.gather_off));
                                btnGather.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.grey, null));
                                btnGather.setEnabled(false);//disabilito pulsante per evitare overload di richieste
                                MainActivity.this.startForegroundService(new Intent(MainActivity.this, LocationService.class));//chiama servizio foreground
                                new Handler().postDelayed(() -> {
                                    btnGather.setEnabled(true);
                                    btnGather.setText(MainActivity.this.getResources().getString(R.string.gather_on));
                                    btnGather.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.purple_500, null));
                                }, 5000);
                            }
                        })
                        .show();
            } else {
                btnGather.setText(this.getResources().getString(R.string.gather_off));
                btnGather.setBackgroundColor(this.getResources().getColor(R.color.grey, null));
                btnGather.setEnabled(false);//disabilito pulsante per evitare overload di richieste
                this.startForegroundService(new Intent(this, LocationService.class));//chiama servizio foreground
                new Handler().postDelayed(() -> {
                    btnGather.setEnabled(true);
                    btnGather.setText(this.getResources().getString(R.string.gather_on));
                    btnGather.setBackgroundColor(this.getResources().getColor(R.color.purple_500, null));
                }, 5000);
            }
        });

        /* MAP */
        mTilesViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())
                .create(TilesViewModel.class);
        mTilesViewModel.getSearchedTiles().observe(this, new Observer<List<MapTile>>() {
            @Override
            public void onChanged(List<MapTile> mapTiles) {
                //Log.d("query search","launched  return"+mapTiles.size());
                renderActiveArea(mapTiles);
            }
        });
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

    }

    private void changeMapType(int type) {
        mapType = type;
        cENABLE = new boolean[]{true, true, true};
        btnMD.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.medium, null));
        queryMapData();
        switch (type) {
            case 0:
            case 1:
                btnSX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.poor, null));
                btnSX.setText(getApplicationContext().getResources().getString(R.string.poor));
                btnSX.setContentDescription(getApplicationContext().getResources().getString(R.string.poor));
                btnDX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.good, null));
                btnDX.setText(getApplicationContext().getResources().getString(R.string.good));
                btnDX.setContentDescription(getApplicationContext().getResources().getString(R.string.good));
                LOW = getApplicationContext().getResources().getColor(R.color.poor, null);
                HIGH = getApplicationContext().getResources().getColor(R.color.good, null);
                LOWT = getApplicationContext().getResources().getColor(R.color.poorT, null);
                HIGHT = getApplicationContext().getResources().getColor(R.color.goodT, null);
                break;
            case 2:
                btnSX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.quiet, null));
                btnSX.setText(getApplicationContext().getResources().getString(R.string.quiet));
                btnSX.setContentDescription(getApplicationContext().getResources().getString(R.string.quiet));
                btnDX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.loud, null));
                btnDX.setText(getApplicationContext().getResources().getString(R.string.loud));
                btnDX.setContentDescription(getApplicationContext().getResources().getString(R.string.loud));
                LOW = getApplicationContext().getResources().getColor(R.color.quiet, null);
                HIGH = getApplicationContext().getResources().getColor(R.color.loud, null);
                LOWT = getApplicationContext().getResources().getColor(R.color.quietT, null);
                HIGHT = getApplicationContext().getResources().getColor(R.color.loudT, null);
                break;
        }
    }

    private void preferenceInit() {
        Map<String, ?> mypref = (Map<String, ?>) defaultPreferences.getAll();
        try {
            if (mypref.get("maxrecorddata") != null) {
                maxTileData = Integer.parseInt((String) Objects.requireNonNull(mypref.get("maxrecorddata")));
            }
            if ((boolean) mypref.get("bgsampling")) {
                if (!Utils.isBackgroundLocationGranted)
                    Utils.requestMyPermission(this, BACKGROUND_PERMISSION_REQUEST_CODE, false);
                else {
                    //lancio bg receiver
                    Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
                    sendBroadcast(i);
                    Log.e("option", "permesso");
                }
            }
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /* START MAP MANAGEMENT */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Override the default content description on the view, for accessibility mode.
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        //googleMap.setContentDescription(getString(R.string.polygon_demo_description));
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setMapType(MAP_TYPE_TERRAIN);
        // Check if permissions are granted, if so, enable the my location layer
        if (Utils.isLocationGranted) {
            map.setMyLocationEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            Utils.requestMyPermission(this, MY_LOCATION_PERMISSION_REQUEST_CODE, false);
        }
        // Move the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.49840, 11.35541), 16));
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                queryMapData();
            }
        });
    }

    private void queryMapData() {
        if (map == null) {
            return;
        }
        map.clear();
        if (map.getCameraPosition().zoom >= 12) {//12?
            areaStatus.setText("");
            VisibleRegion mapView = map.getProjection().getVisibleRegion();
            /*
            map.addPolygon(new PolygonOptions()
                    .addAll(Arrays.asList(new LatLng(map.getCameraPosition().target.latitude - halfHeight, map.getCameraPosition().target.longitude - halfWidth),
                            new LatLng(map.getCameraPosition().target.latitude - halfHeight, map.getCameraPosition().target.longitude + halfWidth),
                            new LatLng(map.getCameraPosition().target.latitude + halfHeight, map.getCameraPosition().target.longitude + halfWidth),
                            new LatLng(map.getCameraPosition().target.latitude + halfHeight, map.getCameraPosition().target.longitude - halfWidth)))
                    .fillColor(evaluate(70))
                    .strokeWidth(0));
            */
            if (mapView.nearLeft.latitude > mapView.farRight.latitude) {
                return;
            }
            double BLlat = customSizeTile(mapView.nearLeft.latitude, true);
            double BLlon = customSizeTile(mapView.nearLeft.longitude, false);
            double TRlat = customSizeTile(mapView.farRight.latitude, true);
            double TRlon = customSizeTile(mapView.farRight.longitude, false);
            //Log.d("BL", String.valueOf(BLlat) + "  " + String.valueOf(BLlon));
            //query al db
            if (mapView.nearLeft.longitude > mapView.farRight.longitude) {//to handle pacman effect
                mTilesViewModel.searchPacmanMapTiles(mapType, BLlat, BLlon, TRlat, TRlon);
            } else {
                mTilesViewModel.searchMapTiles(mapType, BLlat, BLlon, TRlat, TRlon);
            }
        } else {
            areaStatus.setText(R.string.toofar);
        }
    }

    private void renderActiveArea(List<MapTile> storedTiles) {
        map.clear();
        if (storedTiles.isEmpty()) {
            areaStatus.setText(R.string.nodata);
            return;
        }
        areaStatus.setText("");
        class Coppia {
            public int sum;
            public int iter;

            Coppia(int a, int b) {
                this.sum = a;
                this.iter = b;
            }
        }
        List<MapTile> todrop = new ArrayList<>();
        Map<LatLng, Coppia> compressed = new HashMap<LatLng, Coppia>();
        for (MapTile tile : storedTiles) {//processo i record e prendo gli ultimi m (m parametro settato dall'utente)
            Coppia t = compressed.get(new LatLng(tile.latitude, tile.longitude));
            if (t == null) {
                compressed.put(new LatLng(tile.latitude, tile.longitude), new Coppia(tile.level, 1));
            } else {
                if (t.iter + 1 > maxTileData) {//se troppe rilevazioni
                    todrop.add(tile);
                } else {
                    compressed.put(new LatLng(tile.latitude, tile.longitude), new Coppia(t.sum + tile.level, t.iter + 1));
                }
            }
        }
        compressed.forEach((tile, v) -> {//rendering dei tile
            map.addPolygon(new PolygonOptions()
                    .addAll(Arrays.asList(new LatLng(tile.latitude - halfHeight, tile.longitude - halfWidth),
                            new LatLng(tile.latitude - halfHeight, tile.longitude + halfWidth),
                            new LatLng(tile.latitude + halfHeight, tile.longitude + halfWidth),
                            new LatLng(tile.latitude + halfHeight, tile.longitude - halfWidth)))
                    .fillColor(evaluate(v.sum / v.iter))
                    .strokeWidth(0));
        });
        //drop record in eccesso
        for (MapTile tile : todrop) {
            mTilesViewModel.deleteTile(tile);
        }
    }

    static int evaluate(int level) {
        if (cENABLE[2] && level >= 70 && level <= 100) {
            return HIGHT;
        } else if (cENABLE[1] && level >= 35 && level < 70) {
            return AVERAGET;
        } else if (cENABLE[0] && level >= 0 && level < 35) {
            return LOWT;
        } else {
            return NODATA;
        }
    }

    /* END MAP MANAGEMENT */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        changeMapType(Integer.parseInt(getResources().getStringArray(R.array.spinner_category_values)[i]));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("main","RESUMED");
        Utils.checkPermissions(this);
        Log.e("bg PERMISSION", String.valueOf(isBackgroundLocationGranted));
        if(Utils.isLocationGranted) {
            Utils.checkGPS(this);
        }
        if (Utils.isLocationGranted) {
            btnGather.setEnabled(true);
            btnGather.setText(getApplicationContext().getResources().getString(R.string.gather_on));
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.purple_500, null));
            if(map != null) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            }
        } else {
            btnGather.setEnabled(false);
            if(map != null) {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.grey, null));
                btnGather.setEnabled(false);//disabilito pulsante per evitare overload di richieste
                Toast.makeText(this, "SOME FUNCTIONS WILL NOT WORK WITHOUT LOCATION PERMISSION", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Utils.setGrantedPermission(requestCode, permissions, grantResults);
        if (Utils.isLocationGranted) {
            btnGather.setEnabled(true);
            btnGather.setText(getApplicationContext().getResources().getString(R.string.gather_on));
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.purple_500, null));
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            //finish();
            //startActivity(getIntent());
        } else {
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.grey, null));
            btnGather.setEnabled(false);//disabilito pulsante per evitare overload di richieste
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            Toast.makeText(this, "SOME FUNCTIONS WILL NOT WORK WITHOUT LOCATION PERMISSION", Toast.LENGTH_SHORT).show();
        }
    }
}