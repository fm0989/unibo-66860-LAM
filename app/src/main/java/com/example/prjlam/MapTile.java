package com.example.prjlam;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//import com.google.gson.annotations.SerializedName;

@Entity(tableName = "map_tiles_table",primaryKeys = {"latitude","longitude"})
public class MapTile {
    //@SerializedName("title")  retrofit
    @NonNull
    @ColumnInfo(name = "latitude")
    public double latitude;
    @NonNull
    @ColumnInfo(name = "longitude")
    public double longitude;
    @NonNull
    @ColumnInfo(name = "lte")
    public int lte;
    @NonNull
    @ColumnInfo(name = "wifi")
    public int wifi;//0-3
    @NonNull
    @ColumnInfo(name = "noise")
    public int noise;//0-3

    public MapTile(@NonNull double latitude,@NonNull double longitude,@NonNull int lte,@NonNull int wifi,@NonNull int noise)
    {this.lte = lte;this.latitude = latitude;this.longitude = longitude;this.wifi = wifi;this.noise = noise;}
    public double[] getMapTileCoords(){double[] c = {this.latitude, this.longitude};return c;}
    public int getMapTileLte(){return this.lte;}
    public void setMapTileLte(int level){this.lte = level;}
    public int getMapTileWifi(){return this.wifi;}
    public void setMapTileWifi(int level){this.wifi = level;}
    public int getMapTileNoise(){return this.noise;}
    public void setMapTileNoise(int level){this.noise = level;}
}
