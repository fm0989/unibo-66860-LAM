package com.example.prjlam;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.Notification;
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
                    preferenceInit();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        PreferenceManager.setDefaultValues(this, R.xml.myprefs, false);
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

        /* Setting data gathering  */
        btnGather.setOnClickListener(v -> {
            // check permessi
            Utils.checkPermissions(this);
            if (!Utils.isLocationGranted) {
                //this wont happen
                return;
            }
            if(!Utils.isAudioGranted) {
                // notify user
                Utils.requestMyPermission(MainActivity.this,Utils.AUDIO_PERMISSION_REQUEST_CODE,false);
                return;
            }
            startGather();
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

    private void startGather(){
        btnGather.setText(getApplicationContext().getResources().getString(R.string.gather_off));
        btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.grey, null));
        btnGather.setEnabled(false);//disabilito pulsante per evitare overload di richieste
        getApplicationContext().startForegroundService(new Intent(getApplicationContext(), LocationService.class));//chiama servizio foreground
        new Handler().postDelayed(() -> {
            btnGather.setEnabled(true);
            btnGather.setText(MainActivity.this.getResources().getString(R.string.gather_on));
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.purple_500, null));
        }, 5000);
    }
    private void checkReport() {
        /* Create Report Notification Channel */
        String description = getResources().getString(R.string.notifchreportdescr);
        NotificationChannel channel = new NotificationChannel(getResources().getString(R.string.notifchreportid), getResources().getString(R.string.notifchreport), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        if(!notificationManager.areNotificationsEnabled() && Utils.isLocationGranted){
            Utils.requestMyPermission(this,Utils.NOTIFICATION_PERMISSION_REQUEST_CODE,false);
        }
        //NOTIFICA REPORT SE IL GIORNO E' CORRETTO
        int t;
        try {
            t = Integer.parseInt(defaultPreferences.getString("daysreport", "0"));
            if(t > 0) {
                mTilesViewModel.getSearchedRecentTiles().observe(this, new Observer<List<MapTile>>() {
                    @Override
                    public void onChanged(List<MapTile> mapTiles) {
                        List<LatLng> coords = new ArrayList<>();
                        //raggruppa dati
                        for (MapTile tile : mapTiles) {
                            if(!coords.contains(new LatLng(tile.latitude,tile.longitude))){
                                coords.add(new LatLng(tile.latitude,tile.longitude));
                            }
                        }
                        //lancia notifica
                        Log.e("main","lanico notifica");
                        Intent reportIntent = new Intent(getApplicationContext(), MainActivity.class);
                        reportIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        reportIntent.putExtra("CALLER", "notificationFromMain");
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(), getResources().getString(R.string.notifchreportid))
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(getResources().getString(R.string.notifchreporttitle))
                                .setContentText(getResources().getString(R.string.notifchreporttext1)+coords.size()+" "+getResources().getString(R.string.notifchreporttext2))
                                .build();
                        notificationManager.notify(3,notification);
                        defaultPreferences.edit().putLong("reportTime", System.currentTimeMillis()+86400000L*Integer.parseInt(defaultPreferences.getString("daysreport", "0"))).apply();
                    }
                });
                long nowT = System.currentTimeMillis();
                long reportT = defaultPreferences.getLong("reportTime", 0);
                Log.d("MAIN REPORT T", String.valueOf(reportT));
                if (nowT > reportT) {//se la data e' stata superata
                    mTilesViewModel.getNewDiscoveredTiles(reportT);
                }
            }
        } catch (NumberFormatException nfe) {
            defaultPreferences.edit().putString("daysreport", "0").apply();
            defaultPreferences.edit().putLong("reportTime", 0L).apply();
        }
    }

    //modifica l'UI dei bottoni legenda
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
                    Utils.requestMyPermission(this, Utils.BACKGROUND_PERMISSION_REQUEST_CODE, false);
                else {
                    //lancio bg receiver
                    Intent i = new Intent(getApplicationContext(), BackgroundReceiver.class).setAction(getApplicationContext().getResources().getString(R.string.reset_alarm_action));
                    sendBroadcast(i);
                }
            }
            if(mypref.get("reportTime") == null){//primo avvio dell'app
                long reportT = System.currentTimeMillis() + 86400000L * Long.parseLong(defaultPreferences.getString("daysreport", "0"));
                defaultPreferences.edit().putLong("reportTime", reportT).apply();
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
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setMapType(MAP_TYPE_TERRAIN);
        // Check if permissions are granted, if so, enable the my location layer
        if (Utils.isLocationGranted) {
            map.setMyLocationEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            Utils.requestMyPermission(this, Utils.MY_LOCATION_PERMISSION_REQUEST_CODE, false);
        }
        // Move the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.49840, 11.35541), 16));
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() { queryMapData(); }
        });
    }

    private void queryMapData() {
        if (map == null) {
            return;
        }
        map.clear();
        if (map.getCameraPosition().zoom >= 12) {
            areaStatus.setText("");
            VisibleRegion mapView = map.getProjection().getVisibleRegion();
            if (mapView.nearLeft.latitude > mapView.farRight.latitude) {
                return;
            }
            double BLlat = Utils.customSizeTile(mapView.nearLeft.latitude, true);
            double BLlon = Utils.customSizeTile(mapView.nearLeft.longitude, false);
            double TRlat = Utils.customSizeTile(mapView.farRight.latitude, true);
            double TRlon = Utils.customSizeTile(mapView.farRight.longitude, false);
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
                    .addAll(Arrays.asList(new LatLng(tile.latitude - Utils.halfHeight, tile.longitude - Utils.halfWidth),
                            new LatLng(tile.latitude - Utils.halfHeight, tile.longitude + Utils.halfWidth),
                            new LatLng(tile.latitude + Utils.halfHeight, tile.longitude + Utils.halfWidth),
                            new LatLng(tile.latitude + Utils.halfHeight, tile.longitude - Utils.halfWidth)))
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
        } else if (cENABLE[1] && level > 35 && level < 70) {
            return AVERAGET;
        } else if (cENABLE[0] && level >= 0 && level <= 35) {
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
        if (!Utils.isBackgroundLocationGranted) {
            defaultPreferences.edit().putBoolean("bgsampling", false).apply();
        }
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
        /* notifiche */
        checkReport();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Utils.setGrantedPermission(requestCode, permissions, grantResults);
        if (Utils.isLocationGranted && requestCode == Utils.MY_LOCATION_PERMISSION_REQUEST_CODE) {
            btnGather.setEnabled(true);
            btnGather.setText(getApplicationContext().getResources().getString(R.string.gather_on));
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.purple_500, null));
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else if (requestCode == Utils.AUDIO_PERMISSION_REQUEST_CODE) {
            startGather();
        }else if(requestCode == Utils.MY_LOCATION_PERMISSION_REQUEST_CODE){
            btnGather.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.grey, null));
            btnGather.setEnabled(false);
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            Toast.makeText(this, "SOME FUNCTIONS WILL NOT WORK WITHOUT LOCATION PERMISSION", Toast.LENGTH_SHORT).show();
        }
    }
}