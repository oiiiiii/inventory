package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 子分类实体类
 * 对应Room数据库的sub_category表
 */
@Entity(tableName = "sub_category")
public class SubCategory {
    @PrimaryKey(autoGenerate = true)
    private long id;

    // 所属分类ID（必填，long类型本身非空）
    private long categoryId;

    // 子分类名称（必填，标记非空）
    @NonNull
    private String name;

    // 空构造函数（Room要求，保留）
    public SubCategory() {}

    // 带参构造函数（添加@Ignore，消除Room警告）
    @Ignore
    public SubCategory(long categoryId, @NonNull String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    // ==================== Getter/Setter方法（规范且完整） ====================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}