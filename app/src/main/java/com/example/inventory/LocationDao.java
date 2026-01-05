package com.example.inventory;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 位置数据访问接口
 */
@Dao
public interface LocationDao {
    @Insert
    void insert(Location location);

    @Update
    void update(Location location);

    @Delete
    void delete(Location location);

    @Query("SELECT * FROM location ORDER BY name ASC")
    LiveData<List<Location>> getAllLocations();

    @Query("SELECT * FROM location WHERE name = :name LIMIT 1")
    Location getLocationByName(String name);
}