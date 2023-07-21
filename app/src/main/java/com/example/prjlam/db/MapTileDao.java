package com.example.prjlam.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface MapTileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTile(MapTile tile);
    @Delete
    void deleteTile(MapTile tile);
    @Query("DELETE FROM map_tiles_table")
    void deleteAll();
    @Query("DELETE FROM map_tiles_table WHERE type=:type")
    void deleteType(int type);
    @Query("SELECT * FROM map_tiles_table WHERE type=:type AND latitude>=:minlatitude AND latitude<=:maxlatitude AND longitude>=:minlongitude AND longitude<=:maxlongitude ORDER BY date DESC")
    List<MapTile> getTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude);
    @Query("SELECT * FROM map_tiles_table WHERE type=:type AND latitude>=:minlatitude AND latitude<=:maxlatitude AND ((longitude>=:minlongitude AND longitude<180) OR " +
            "(longitude<=:maxlongitude AND longitude>-180)) ORDER BY date DESC")
    List<MapTile> getPacmanPointTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude);
    @Query("SELECT * FROM map_tiles_table as A WHERE NOT EXISTS (SELECT * FROM map_tiles_table as B WHERE A.longitude=B.longitude AND A.latitude=B.latitude AND date < :starting)")
    List<MapTile> getNewDiscoveredTiles(long starting);
    @RawQuery
    int checkpoint(SupportSQLiteQuery supportSQLiteQuery);
}
