package com.example.inventory;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 子分类数据访问接口
 */
@Dao
public interface SubCategoryDao {
    @Insert
    void insert(SubCategory subCategory);

    @Update
    void update(SubCategory subCategory);

    @Delete
    void delete(SubCategory subCategory);

    @Query("SELECT * FROM sub_category WHERE categoryId = :categoryId ORDER BY name ASC")
    LiveData<List<SubCategory>> getSubCategoriesByCategoryId(long categoryId);

    @Query("SELECT * FROM sub_category WHERE name = :name AND categoryId = :categoryId LIMIT 1")
    SubCategory getSubCategoryByName(long categoryId, String name);
}