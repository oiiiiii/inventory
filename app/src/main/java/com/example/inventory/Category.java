package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore; // 导入Ignore注解
import androidx.room.PrimaryKey;

/**
 * 分类实体类
 * 对应Room数据库的category表
 */
@Entity(tableName = "category")
public class Category {
    // 自增主键（long类型本身非空，无需@NonNull）
    @PrimaryKey(autoGenerate = true)
    private long id;

    // 分类名称（唯一，必填，标记非空）
    @NonNull
    private String name;

    // 空构造函数（Room要求，保留）
    public Category() {}

    // 带参构造函数（添加@Ignore，告诉Room忽略该构造函数）
    @Ignore // 核心修复：添加该注解消除警告
    public Category(@NonNull String name) {
        this.name = name;
    }

    // ==================== Getter/Setter方法（完整且规范） ====================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}