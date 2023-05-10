package com.example.prjlam;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    ImageButton btnSett;
    Button btnSX,btnMD,btnDX;
    TextView areaStatus;
    Spinner spCat;
    SharedPreferences defaultPreferences;
    private TileViewModel mTileViewModel;
    /* MAPS SETS */
    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static boolean isLocationPermissionGranted = false;// ???
    private GoogleMap map;
    double NTILES = 80000;
    double halfWidth = 360/NTILES/4;//360 LONG
    double halfHeight = 180/NTILES/3;//180 LAT
    private int mapType;
    private int maxTileData = 4;
    private static final int NODATA = Color.argb(0,170,170,170);
    private static final int LOW = Color.argb(80,0,255,0);
    private static final int AVERAGE = Color.argb(80,255,255,0);
    private static final int HIGH = Color.argb(80,255,0,0);


    ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(//API di activity result sistema piu' sicuro a contratto
    new ActivityResultContracts.StartActivityForResult(),
    new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.d("main","tornato");
            preferenceInit();
            if (result.getResultCode() == Activity.RESULT_OK)
            {

            }
            //
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Create the Notification Channel */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notificationServiceChannel";
            String description = "Whatever";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        setContentView(R.layout.activity_main);
        btnSX = findViewById(R.id.btnSX);
        btnMD = findViewById(R.id.btnMD);
        btnDX = findViewById(R.id.btnDX);
        areaStatus = findViewById(R.id.dataStatus);
        /* SET DEFAULT CATEGORY */
        changeMapType(0);
        /* SET PREFERENCES */
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        preferenceInit();

        btnSett = findViewById(R.id.btnSettings);
        btnSett.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), OptionActivity.class);
            mLauncher.launch(i);
        });

        /* Spinner */
        spCat = findViewById(R.id.category);
        spCat.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_category, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spCat.setAdapter(spinnerAdapter);

        /* Start gathering info's
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), GatheringService.class));
        } else {
            getApplicationContext().startService(new Intent(getApplicationContext(), GatheringService.class));
        }
        */

        /* MAPS */
        mTileViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())
                .create(TileViewModel.class);
        mTileViewModel.getSearchedTiles().observe(this, new Observer<List<MapTile>>() {
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
        mapType=type;
        switch(type){
            case 0:
            case 1:
                btnSX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.poor));
                btnSX.setText(getApplicationContext().getResources().getString(R.string.poor));
                btnSX.setContentDescription(getApplicationContext().getResources().getString(R.string.poor));
                btnDX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.good));
                btnDX.setText(getApplicationContext().getResources().getString(R.string.good));
                btnDX.setContentDescription(getApplicationContext().getResources().getString(R.string.good));
                break;
            case 2:
                btnSX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.quiet));
                btnSX.setText(getApplicationContext().getResources().getString(R.string.quiet));
                btnSX.setContentDescription(getApplicationContext().getResources().getString(R.string.quiet));
                btnDX.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.loud));
                btnDX.setText(getApplicationContext().getResources().getString(R.string.loud));
                btnDX.setContentDescription(getApplicationContext().getResources().getString(R.string.loud));
                break;
        }
    }

    private void preferenceInit() {
        Map<String, String> mypref = (Map<String, String>) defaultPreferences.getAll();
        maxTileData = Integer.parseInt(mypref.get("maxrecorddata"));
    }

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
        //map.setMinZoomPreference(14);
        // Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            isLocationPermissionGranted = true;
        } else {//if permissions are not granted
            PermissionUtils.requestLocationPermissions(this,MY_LOCATION_PERMISSION_REQUEST_CODE,false);
            map.setMyLocationEnabled(false);
            isLocationPermissionGranted = false;
        }

        // Move the map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.49840,11.35541), 16));
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(map.getCameraPosition().zoom >= 14) {//14?
                    areaStatus.setText("");
                    VisibleRegion mapView = map.getProjection().getVisibleRegion();
                    if (mapView.nearLeft.longitude > mapView.farRight.longitude || mapView.nearLeft.latitude > mapView.farRight.latitude) {
                        return;
                    }
                    double BLlat = truncate(Math.round((mapView.nearLeft.latitude + 90 - halfHeight) / 2 / halfHeight) * 2 * halfHeight - 90 + halfHeight, 5);
                    double BLlon = truncate(Math.round((mapView.nearLeft.longitude + 180 - halfWidth) / 2 / halfWidth) * 2 * halfWidth - 180 + halfWidth, 5);
                    double TRlat = truncate(Math.round((mapView.farRight.latitude + 90 - halfHeight) / 2 / halfHeight) * 2 * halfHeight - 90 + halfHeight, 5);
                    double TRlon = truncate(Math.round((mapView.farRight.longitude + 180 - halfWidth) / 2 / halfWidth) * 2 * halfWidth - 180 + halfWidth, 5);
                    //query al db
                    mTileViewModel.searchMapTiles(mapType, BLlat, BLlon, TRlat, TRlon);
                }else{
                    areaStatus.setText(R.string.toofar);
                }
            }
        });
    }
    private List<LatLng> createArea(LatLng center) {
        return Arrays.asList(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth));
    }
    private void renderActiveArea(List<MapTile> storedTiles){
        map.clear();
        if(storedTiles.isEmpty()){
            areaStatus.setText(R.string.nodata);
            return;
        }
        areaStatus.setText("");
        class Coppia{
            public int sum;
            public int iter;
            Coppia(int a,int b){this.sum=a;this.iter=b;}
        }
        List<MapTile> todrop = new ArrayList<>();
        Map<LatLng, Coppia> compressed = new HashMap<LatLng, Coppia>();
        for (MapTile tile : storedTiles){//processo i record e prendo gli ultimi m (m parametro settato dall'utente)
            Coppia t = compressed.get(new LatLng(tile.latitude,tile.longitude));
            if(t == null){
                compressed.put(new LatLng(tile.latitude,tile.longitude),new Coppia(tile.level,1));
            }else{
                if(t.iter+1>maxTileData) {//se troppe rilevazioni
                    todrop.add(tile);
                }else {
                    compressed.put(new LatLng(tile.latitude, tile.longitude), new Coppia(t.sum+tile.level, t.iter+1));
                }
            }
        }
        compressed.forEach((tile,v) -> {//rendering dei tile
            map.addPolygon(new PolygonOptions()
                    .addAll(createArea(new LatLng(tile.latitude,tile.longitude)))
                    .fillColor(evaluate(v.sum/v.iter))
                    .strokeWidth(0));
        });
        //drop record in eccesso
        for (MapTile tile : todrop){
            mTileViewModel.deleteTile(tile);
        }
        /*
        map.addPolygon(new PolygonOptions()
                    .addAll(createArea(new LatLng(map.getCameraPosition().target.latitude,map.getCameraPosition().target.longitude)))
                    .fillColor(evaluate(70))
                    .strokeWidth(0));
         */
    }
    static double truncate(double value, int decimalpoint)
    {
        value = value * Math.pow(10, decimalpoint);
        value = Math.round(value);
        value = value / Math.pow(10, decimalpoint);
        return value;
    }
    static int evaluate(int level)//0-100
    {
        if(level>=70){
            return HIGH;
        } else if (level>=35) {
            return AVERAGE;
        }else if (level>0){
            return LOW;
        }else{
            return NODATA;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        changeMapType(Integer.parseInt(getResources().getStringArray(R.array.spinner_category_values)[i]));
        if(map!=null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target,map.getCameraPosition().zoom));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
            // Enable the My Location button if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                    .isPermissionGranted(permissions, grantResults,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                map.getUiSettings().setMyLocationButtonEnabled(true);
                isLocationPermissionGranted = true;
            } else {
                map.getUiSettings().setMyLocationButtonEnabled(false);
                isLocationPermissionGranted = false;
            }

        }
    }

}