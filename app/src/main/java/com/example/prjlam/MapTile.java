package com.example.prjlam;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//import com.google.gson.annotations.SerializedName;

@Entity(tableName = "map_tiles_table",primaryKeys = {"latitude","longitude","type","date"})
public class MapTile {
    //@SerializedName("title")  retrofit
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
    public int date;

    public MapTile(@NonNull double latitude,@NonNull double longitude,@NonNull int level,@NonNull int type,@NonNull int date)
    {this.latitude = latitude;this.longitude = longitude;this.level = level;this.type=type;this.date=date;}
    public double[] getMapTileCoords(){double[] c = {this.latitude, this.longitude};return c;}
    public int getMapTileLevel(){return this.level;}
    public void setMapTileLevel(int level){this.level = level;}
    public int getMapTileType(){return this.type;}
    public void setMapTileType(int type){this.type = date;}
    public int getMapTileDate(){return this.date;}
    public void setMapTileDate(int date){this.date = date;}
}
