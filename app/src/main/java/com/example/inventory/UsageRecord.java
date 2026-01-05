package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 使用记录实体类
 * 对应Room数据库的usage_record表
 */
@Entity(tableName = "usage_record")
public class UsageRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;

    // 关联的物品ID（UUID，必填，标记非空）
    @NonNull
    private String itemId;

    // 操作类型：create（创建）、update（修改）（必填，标记非空）
    @NonNull
    private String type;

    // 操作时间（格式：yyyy.MM.dd HH:mm，必填，标记非空）
    @NonNull
    private String time;

    // 修改的字段（如：name,category；create类型时为空，可选）
    private String modifiedFields;

    // 空构造函数（Room要求，必须保留）
    public UsageRecord() {}

    // 带参构造函数（添加@Ignore，消除Room构造函数歧义警告）
    @Ignore
    public UsageRecord(@NonNull String itemId, @NonNull String type, @NonNull String time, String modifiedFields) {
        this.itemId = itemId;
        this.type = type;
        this.time = time;
        this.modifiedFields = modifiedFields;
    }

    // ==================== Getter/Setter方法（规范且完整） ====================
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull String itemId) {
        this.itemId = itemId;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getTime() {
        return time;
    }

    public void setTime(@NonNull String time) {
        this.time = time;
    }

    public String getModifiedFields() {
        return modifiedFields;
    }

    public void setModifiedFields(String modifiedFields) {
        this.modifiedFields = modifiedFields;
    }
}