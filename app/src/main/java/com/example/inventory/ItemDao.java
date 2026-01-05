package com.example.inventory;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 物品数据访问接口
 */
@Dao
public interface ItemDao {
    // 插入单条物品
    @Insert
    void insert(Item item);

    // 更新物品
    @Update
    void update(Item item);

    // 删除物品
    @Delete
    void delete(Item item);

    // 根据ID查询物品
    @Query("SELECT * FROM item WHERE id = :itemId")
    LiveData<Item> getItemById(String itemId);

    // 查询所有物品（按创建时间倒序）
    @Query("SELECT * FROM item ORDER BY createTime DESC")
    LiveData<List<Item>> getAllItems();

    // 分页查询物品（按创建时间倒序）
    @Query("SELECT * FROM item ORDER BY createTime DESC LIMIT :pageSize OFFSET :offset")
    LiveData<List<Item>> getItemsByPage(int pageSize, int offset);

    // 模糊查询物品名称（分页）
    @Query("SELECT * FROM item WHERE name LIKE '%' || :keyword || '%' ORDER BY createTime DESC LIMIT :pageSize OFFSET :offset")
    LiveData<List<Item>> searchItemsByName(String keyword, int pageSize, int offset);

    // 筛选临期/过期物品（分页）—— 修复SQL参数绑定语法
    @Query("SELECT * FROM item WHERE expiryDate IS NOT NULL AND expiryDate != '' AND " +
            "strftime('%s', expiryDate) <= strftime('%s', datetime('now', '+' || :days || ' day')) " +
            "ORDER BY expiryDate ASC LIMIT :pageSize OFFSET :offset")
    LiveData<List<Item>> getExpiringItems(int days, int pageSize, int offset);

    // 查询过期物品（分页）
    @Query("SELECT * FROM item WHERE expiryDate IS NOT NULL AND expiryDate != '' AND " +
            "strftime('%s', expiryDate) < strftime('%s', 'now') " +
            "ORDER BY expiryDate ASC LIMIT :pageSize OFFSET :offset")
    LiveData<List<Item>> getExpiredItems(int pageSize, int offset);
}