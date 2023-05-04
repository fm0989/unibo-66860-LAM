package com.example.prjlam;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    ImageButton btnZin,btnZout,btnSett;
    Spinner spCat;
    SharedPreferences defaultPreferences;
    /* MAPS SETS */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap map;
    double NTILES = 100000;
    double halfWidth = 180/NTILES/2;//360/2 LONG
    double halfHeight = 180/NTILES/2;//180 LAT
    private TileViewModel mTileViewModel;
    private List<LatLng> rendertiles= new LinkedList<>();
    private int mapType=0;//0-2
    private static final int NODATA = Color.argb(0,170,170,170);
    private static final int LOW = Color.argb(80,0,255,0);
    private static final int AVERAGE = Color.argb(80,255,255,0);
    private static final int HIGH = Color.argb(80,255,0,0);


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
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //Log.d("defaultPref",defaultPreferences.getAll().toString());

        btnZin = findViewById(R.id.zoomIn);
        btnZout = findViewById(R.id.zoomOut);
        btnSett = findViewById(R.id.btnSettings);
        btnZin.setOnClickListener(v -> {
            Log.d("zoomin","work!");
        });
        btnZout.setOnClickListener(v -> {
            Log.d("zoomout","work!");
        });
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Override the default content description on the view, for accessibility mode.
        map = googleMap;
        //googleMap.setContentDescription(getString(R.string.polygon_demo_description));
        map.setIndoorEnabled(false);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setMapType(MAP_TYPE_TERRAIN);
        map.setMinZoomPreference(14);
        map.setLatLngBoundsForCameraTarget(new LatLngBounds(new LatLng(-85,-179),new LatLng(85,179)));//bound per evitare problemi di search
        // Enable the location layer. Request the location permission if needed.
        /*
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            // Uncheck the box until the layer has been enabled and request missing permission.
            PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE,true);
            map.setMyLocationEnabled(false);
        }*/

        // Move the map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.49840,11.35541), 16));
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                VisibleRegion mapView = map.getProjection().getVisibleRegion();
                double BLlat = truncate(Math.round((mapView.nearLeft.latitude+90-halfHeight)/2/halfHeight)*2*halfHeight-90+halfHeight,5);
                double BLlon = truncate(Math.round((mapView.nearLeft.longitude+180-halfWidth)/2/halfWidth)*2*halfWidth-180+halfWidth,5);
                double TRlat = truncate(Math.round((mapView.farRight.latitude+90-halfHeight)/2/halfHeight)*2*halfHeight-90+halfHeight,5);
                double TRlon = truncate(Math.round((mapView.farRight.longitude+180-halfWidth)/2/halfWidth)*2*halfWidth-180+halfWidth,5);

                //popola i tile da renderizzare
                rendertiles.clear();
                int mi = (int) (Math.abs(Math.round((mapView.farRight.latitude+90-halfHeight)/2/halfHeight) - Math.round((mapView.nearLeft.latitude+90-halfHeight)/2/halfHeight))+1);
                int mj = (int) (Math.abs(Math.round((mapView.farRight.longitude+180-halfWidth)/2/halfWidth) - Math.round((mapView.nearLeft.longitude+180-halfWidth)/2/halfWidth))+1);
                for(int i=0;i<mi;i++){
                    for(int j=0;j<mj;j++){
                        rendertiles.add(new LatLng(truncate(BLlat + halfHeight*2*i,5),truncate(BLlon + halfWidth*2*j,5)));
                    }
                }
                //query al db
                mTileViewModel.searchMapTiles(BLlat,BLlon,TRlat,TRlon);
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
        int tilecolor = NODATA;
        class Coppia{
            public int sum;
            public int iter;
            Coppia(int a,int b){this.sum=a;this.iter=b;}
        }
        Map<LatLng, Coppia> compressed = new HashMap<LatLng, Coppia>();
        for (MapTile tile : storedTiles){//faccio rendering dei tile presi dal db
            Coppia t = compressed.get(new LatLng(tile.latitude,tile.longitude));
            if(t != null){
                //compressed.put(new LatLng(tile.latitude,tile.longitude),new Coppia(t.sum+tile.));
            }
            switch (mapType) {
                case 0: tilecolor = evaluate(tile.lte);
                    break;
                case 1: tilecolor = evaluate(tile.wifi);
                    break;
                case 2: tilecolor = evaluate(tile.noise);
                    break;
            }
            rendertiles.remove(new LatLng(tile.latitude,tile.longitude));
            map.addPolygon(new PolygonOptions()
                    .addAll(createArea(new LatLng(tile.latitude,tile.longitude)))
                    .fillColor(tilecolor)
                    .strokeWidth(0));
        }

        /*
        for (LatLng untracked : rendertiles){//rendering dei tile mancanti
            map.addPolygon(new PolygonOptions()
                    .addAll(createArea(new LatLng(untracked.latitude,untracked.longitude)))
                    .fillColor(NODATA)
                    .strokeWidth(0));
        }
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
        if(level>70){
            return HIGH;
        } else if (level>35) {
            return AVERAGE;
        }else if (level>0){
            return LOW;
        }else{
            return NODATA;
        }
    }
}