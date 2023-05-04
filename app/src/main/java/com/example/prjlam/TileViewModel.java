package com.example.prjlam;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class TileViewModel extends AndroidViewModel {

    private MapRepository mRepository;
    private MutableLiveData<List<MapTile>> searchedMapTiles;


    public TileViewModel(@NonNull Application application) {
        super(application);
        mRepository = new MapRepository(application);
        searchedMapTiles = mRepository.getSearchResults();
    }

    MutableLiveData<List<MapTile>> getSearchedTiles(){
        return searchedMapTiles;
    }
    public void searchMapTiles(double minlatitude, double minlongitude, double maxlatitude, double maxlongitude){
        mRepository.searchMapTiles(minlatitude, minlongitude, maxlatitude, maxlongitude);
    }
    public void insertTiles(List<MapTile> tiles){this.mRepository.insertTiles(tiles);}

}
