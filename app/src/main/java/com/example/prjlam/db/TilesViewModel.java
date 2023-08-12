package com.example.prjlam.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;

public class TilesViewModel extends AndroidViewModel {

    private MapRepository mRepository;
    private MutableLiveData<List<MapTile>> checkedTile;

    private MutableLiveData<List<MapTile>> searchedMapTiles;
    private MutableLiveData<List<MapTile>> searchedRecentMapTiles;


    public TilesViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MapRepository(application);
        searchedMapTiles = mRepository.getSearchResults();
        searchedRecentMapTiles = mRepository.getSearchRecentResults();
        checkedTile = mRepository.getCheckedResults();
    }

    public MutableLiveData<List<MapTile>> getSearchedTiles(){
        return searchedMapTiles;
    }
    public MutableLiveData<List<MapTile>> getSearchedRecentTiles(){
        return searchedRecentMapTiles;
    }
    public MutableLiveData<List<MapTile>> getChekedTile(){
        return checkedTile;
    }

    public void searchMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude){
        mRepository.searchMapTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude);
    }
    public void searchPacmanMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude){
        mRepository.searchPacmanMapTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude);
    }
    public void checkTileEntry(double latitude, double longitude){
        mRepository.checkTileEntry(latitude, longitude);
    }
    public void insertTile(MapTile tile){this.mRepository.insertTile(tile);}
    public void deleteTile(MapTile tile){this.mRepository.deleteTile(tile);}
    public void deleteAll(){this.mRepository.deleteAll();}
    public void deleteType(int type){this.mRepository.deleteType(type);}
    public void getNewDiscoveredTiles(long starting){this.mRepository.getNewDiscoveredTiles(starting);}
    public void checkpointDatabase(){ this.mRepository.checkpointDatabase(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));}

}
