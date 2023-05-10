package com.example.prjlam;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(version = 1, entities = {MapTile.class}, exportSchema = false)
public abstract class MapRoomDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "map_database";
    private static final String DATABASE_BACKUP_SUFFIX = "-bkp";
    private static final String SQLITE_WALFILE_SUFFIX = "-wal";
    private static final String SQLITE_SHMFILE_SUFFIX = "-shm";
    abstract public MapTileDao mapTileDao();

    private static volatile MapRoomDatabase INSTANCE;
    private static final int nTHREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(nTHREADS);
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                //fare inizializzazioni
                MapTileDao dao = INSTANCE.mapTileDao();
                dao.deleteAll();
                dao.insertTile(new MapTile(44.4969,11.3556,20,0,1683211200));
                dao.insertTile(new MapTile(44.4969,11.3556,100,0,1683211233));
            });
        }
    };

    static MapRoomDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (MapRoomDatabase.class) {
                //context.deleteDatabase("map_database");
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MapRoomDatabase.class, DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }
}
