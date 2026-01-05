package com.example.inventory;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * 使用记录数据访问接口
 */
@Dao
public interface UsageRecordDao {
    @Insert
    void insert(UsageRecord record);

    // 根据物品ID查询所有记录（按时间倒序）
    @Query("SELECT * FROM usage_record WHERE itemId = :itemId ORDER BY time DESC")
    LiveData<List<UsageRecord>> getRecordsByItemId(String itemId);

    // 删除物品关联的所有记录
    @Query("DELETE FROM usage_record WHERE itemId = :itemId")
    void deleteRecordsByItemId(String itemId);
}