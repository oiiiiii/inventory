package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;
/**
 * 物品实体类
 * 对应Room数据库的item表
 */
@Entity(tableName = "item")
public class Item {
    // UUID作为主键，唯一标识物品（强制非空）
    @PrimaryKey
    @NonNull
    private String id = UUID.randomUUID().toString();

    // 物品名称（必填，非空）
    @NonNull
    private String name;

    // 分类（可选，可空）
    private String category;

    // 子分类（可选，可空）
    private String subCategory;

    // 位置（可选，可空）
    private String location;

    // 数量（基本数据类型，默认0，非空）
    private int quantity;

    // 有效期（格式：yyyy-MM-dd，可选，可空）
    private String expiryDate;

    // 说明（可选，可空）
    private String description;

    // 图片路径列表（用逗号分隔，可选，可空）
    private String imagePaths;

    // 创建时间（必填，非空）
    @NonNull
    private String createTime;

    // 最后修改时间（必填，非空）
    @NonNull
    private String updateTime;

    // 空构造函数（Room要求，必须保留）
    public Item() {}

    // 全参构造函数（添加@Ignore，Room忽略该构造函数）
    @Ignore
    public Item(@NonNull String id, @NonNull String name, String category, String subCategory,
                String location, int quantity, String expiryDate, String description,
                String imagePaths, @NonNull String createTime, @NonNull String updateTime) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.location = location;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.description = description;
        this.imagePaths = imagePaths;
        // 默认初始化时间
        long currentTime = System.currentTimeMillis();
        this.createTime = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.getDefault()).format(currentTime);
        this.updateTime = this.createTime;
    }

    // ==================== 所有字段的Getter/Setter方法（必须完整） ====================
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    // category字段的Getter/Setter（修复核心）
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // subCategory字段的Getter/Setter
    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    // location字段的Getter/Setter
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // quantity字段的Getter/Setter
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // expiryDate字段的Getter/Setter
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    // description字段的Getter/Setter
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // imagePaths字段的Getter/Setter
    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }

    @NonNull
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(@NonNull String createTime) {
        this.createTime = createTime;
    }

    @NonNull
    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(@NonNull String updateTime) {
        this.updateTime = updateTime;
    }
}