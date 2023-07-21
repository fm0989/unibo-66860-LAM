package com.example.prjlam.db;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;

public class MapRepository {

    private MapTileDao mMapTileDao;
    private MutableLiveData<List<MapTile>> searchedMapTiles = new MutableLiveData<>();
    private MutableLiveData<List<MapTile>> searchedRecentMapTiles = new MutableLiveData<>();

    MapRepository (Application app) {
        MapRoomDatabase db = MapRoomDatabase.getDatabase(app);
        mMapTileDao = db.mapTileDao();
    }

    MutableLiveData<List<MapTile>> getSearchResults() {
        return searchedMapTiles;
    }
    MutableLiveData<List<MapTile>> getSearchRecentResults() {
        return searchedRecentMapTiles;
    }
    // LiveData queries within Room are automatically executed in background
    void searchMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude) {
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
           searchedMapTiles.postValue(mMapTileDao.getTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude));
        });
    }
    void searchPacmanMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude) {
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
           searchedMapTiles.postValue(mMapTileDao.getPacmanPointTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude));
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
    void deleteType(int type){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMapTileDao.deleteType(type);
        });
    }
    void getNewDiscoveredTiles(long starting){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            searchedRecentMapTiles.postValue(mMapTileDao.getNewDiscoveredTiles(starting));
        });
    }
    void checkpointDatabase(SimpleSQLiteQuery simpleSQLiteQuery){
        MapRoomDatabase.databaseWriteExecutor.execute(() -> {
            mMapTileDao.checkpoint(simpleSQLiteQuery);
        });
    }
}
