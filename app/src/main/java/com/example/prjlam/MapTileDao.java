package com.example.prjlam;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MapTileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTile(MapTile tile);
    @Delete
    void deleteTile(MapTile tile);
    @Query("DELETE FROM map_tiles_table")
    void deleteAll();
    @Query("SELECT * FROM map_tiles_table WHERE type=:type AND latitude>=:minlatitude AND latitude<=:maxlatitude AND longitude>=:minlongitude AND longitude<=:maxlongitude ORDER BY date DESC")
    List<MapTile> getTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude);
    @Query("SELECT * FROM map_tiles_table WHERE type=:type AND latitude>=:minlatitude AND latitude<180 AND longitude>=:minlongitude AND longitude<=90 OR " +
            "latitude>-180 AND latitude<=:maxlatitude AND longitude>=:minlongitude AND longitude<=:maxlongitude ORDER BY date DESC")
    List<MapTile> getDeadPointTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude);
}
