package com.example.prjlam.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "map_tiles_table",primaryKeys = {"latitude","longitude","type","date"})
public class MapTile {
    @NonNull
    @ColumnInfo(name = "latitude")
    public double latitude;
    @NonNull
    @ColumnInfo(name = "longitude")
    public double longitude;
    @NonNull
    @ColumnInfo(name = "level")
    public int level;
    @NonNull
    @ColumnInfo(name = "type")
    public int type;
    @ColumnInfo(name = "date")
    public long date;

    public MapTile(@NonNull double latitude,@NonNull double longitude,@NonNull int level,@NonNull int type,@NonNull long date)
    {this.latitude = latitude;this.longitude = longitude;this.level = level;this.type=type;this.date=date;}
    public double[] getMapTileCoords(){double[] c = {this.latitude, this.longitude};return c;}
    public int getMapTileLevel(){return this.level;}
    public void setMapTileLevel(int level){this.level = level;}
    public int getMapTileType(){return this.type;}
    public void setMapTileType(int type){this.type = type;}
    public long getMapTileDate(){return this.date;}
    public void setMapTileDate(int date){this.date = date;}
}
