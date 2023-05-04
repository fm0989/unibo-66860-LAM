package com.example.prjlam;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MapTileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTile(MapTile tile);//forse togliere
    @Update
    void updateTile(MapTile tile);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTiles(List<MapTile> tiles);
    @Query("DELETE FROM map_tiles_table")
    void deleteAll();
    @Query("SELECT * FROM map_tiles_table WHERE latitude>=:minlatitude AND latitude<=:maxlatitude AND longitude>=:minlongitude AND longitude<=:maxlongitude")
    List<MapTile> getTiles(double minlatitude, double minlongitude, double maxlatitude, double maxlongitude);
}
