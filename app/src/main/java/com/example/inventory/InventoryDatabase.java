package com.example.inventory;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 库存管理应用的Room数据库实例
 * 版本号初始为1，后续修改表结构需递增并处理迁移
 */
@Database(entities = {Item.class, Category.class, SubCategory.class, Location.class, UsageRecord.class},
        version = 1, exportSchema = false)
public abstract class InventoryDatabase extends RoomDatabase {
    // 单例实例
    private static volatile InventoryDatabase INSTANCE;

    // DAO接口获取方法
    public abstract ItemDao itemDao();
    public abstract CategoryDao categoryDao();
    public abstract SubCategoryDao subCategoryDao();
    public abstract LocationDao locationDao();
    public abstract UsageRecordDao usageRecordDao();

    // 获取单例
    public static InventoryDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (InventoryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            InventoryDatabase.class,
                            "inventory_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}