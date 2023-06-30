package com.example.prjlam.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class TilesViewModel extends AndroidViewModel {

    private MapRepository mRepository;
    private MutableLiveData<List<MapTile>> searchedMapTiles;


    public TilesViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MapRepository(application);
        searchedMapTiles = mRepository.getSearchResults();
    }

    public MutableLiveData<List<MapTile>> getSearchedTiles(){
        return searchedMapTiles;
    }
    public void searchMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude){
        mRepository.searchMapTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude);
    }
    public void searchPacmanMapTiles(int type,double minlatitude, double minlongitude, double maxlatitude, double maxlongitude){
        mRepository.searchPacmanMapTiles(type, minlatitude, minlongitude, maxlatitude, maxlongitude);
    }
    public void insertTile(MapTile tile){this.mRepository.insertTile(tile);}
    public void deleteTile(MapTile tile){this.mRepository.deleteTile(tile);}
    public void deleteAll(){this.mRepository.deleteAll();}

}
