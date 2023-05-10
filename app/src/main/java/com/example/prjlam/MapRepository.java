package com.example.prjlam;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class MapRepository {

    private MapTileDao mMapTileDao;
    private MutableLiveData<List<MapTile>> searchedMapTiles = new MutableLiveData<>();

    MapRepository (Application app) {
        MapRoomDatabase db = MapRoomDatabase.getDatabase(app);
        mMapTileDao = db.mapTileDao();
    }

    public MutableLiveData<List<MapTile>> getSearchResults() {
        return searchedMapTiles;
    }
    // LiveData queries within Room are automatically executed in background
    void searchMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude) {
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
           searchedMapTiles.postValue(mMapTileDao.getTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude));
        });
    }
    // This does not involve LiveData, therefore we need to explicitly run a separate thread
    void insertTile(MapTile tile){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMapTileDao.insertTile(tile);
        });
    }
    void deleteTile(MapTile tile){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMapTileDao.deleteTile(tile);
        });
    }
    void deleteAll(){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMapTileDao.deleteAll();
        });
    }
}
