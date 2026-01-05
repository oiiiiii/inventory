package com.example.inventory;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;

/**
 * 数据仓库：封装所有数据库操作，提供统一API给ViewModel
 */
public class InventoryRepository {
    // 5个DAO实例
    private ItemDao itemDao;
    private CategoryDao categoryDao;
    private SubCategoryDao subCategoryDao;
    private LocationDao locationDao;
    private UsageRecordDao usageRecordDao;

    // 全局可用的LiveData（如所有分类、所有位置）
    private LiveData<List<Category>> allCategories;
    private LiveData<List<Location>> allLocations;

    // 构造函数：初始化数据库和DAO
    public InventoryRepository(Application application) {
        // 获取Room数据库实例
        InventoryDatabase database = InventoryDatabase.getInstance(application);
        // 初始化DAO
        itemDao = database.itemDao();
        categoryDao = database.categoryDao();
        subCategoryDao = database.subCategoryDao();
        locationDao = database.locationDao();
        usageRecordDao = database.usageRecordDao();
        // 初始化全局LiveData
        allCategories = categoryDao.getAllCategories();
        allLocations = locationDao.getAllLocations();
    }

    // ==================== 物品相关操作 ====================
    // 插入物品（含创建记录）
    public void insertItem(Item item, String createTime) {
        // 生成UUID作为物品ID
        item.setId(UUID.randomUUID().toString());
        item.setCreateTime(createTime);
        item.setUpdateTime(createTime);
        // 插入物品到数据库
        new InsertItemAsyncTask(itemDao, usageRecordDao).execute(item, createTime);
    }

    // 更新物品（含修改记录）
    public void updateItem(Item oldItem, Item newItem, String updateTime, String modifiedFields) {
        newItem.setId(oldItem.getId()); // 保留原ID
        newItem.setCreateTime(oldItem.getCreateTime()); // 保留创建时间
        newItem.setUpdateTime(updateTime); // 更新修改时间
        // 执行更新 + 插入修改记录
        new UpdateItemAsyncTask(itemDao, usageRecordDao).execute(newItem, updateTime, modifiedFields);
    }

    // 删除物品（含关联的使用记录）
    public void deleteItem(Item item) {
        new DeleteItemAsyncTask(itemDao, usageRecordDao).execute(item);
    }

    // 根据ID查询物品
    public LiveData<Item> getItemById(String itemId) {
        return itemDao.getItemById(itemId);
    }

    // 分页查询所有物品
    public LiveData<List<Item>> getItemsByPage(int pageSize, int offset) {
        return itemDao.getItemsByPage(pageSize, offset);
    }

    // 模糊查询物品（按名称）
    public LiveData<List<Item>> searchItemsByName(String keyword, int pageSize, int offset) {
        return itemDao.searchItemsByName(keyword, pageSize, offset);
    }

    // 查询临期物品
    public LiveData<List<Item>> getExpiringItems(int days, int pageSize, int offset) {
        return itemDao.getExpiringItems(days, pageSize, offset);
    }

    // 查询过期物品
    public LiveData<List<Item>> getExpiredItems(int pageSize, int offset) {
        return itemDao.getExpiredItems(pageSize, offset);
    }

    // ==================== 分类相关操作 ====================
    public void insertCategory(Category category) {
        new InsertCategoryAsyncTask(categoryDao).execute(category);
    }

    public void updateCategory(Category category) {
        new UpdateCategoryAsyncTask(categoryDao).execute(category);
    }

    public void deleteCategory(Category category) {
        new DeleteCategoryAsyncTask(categoryDao).execute(category);
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public Category getCategoryByName(String name) {
        // 同步查询（非LiveData，需在IO线程调用）
        return categoryDao.getCategoryByName(name);
    }

    // ==================== 子分类相关操作 ====================
    public void insertSubCategory(SubCategory subCategory) {
        new InsertSubCategoryAsyncTask(subCategoryDao).execute(subCategory);
    }

    public void updateSubCategory(SubCategory subCategory) {
        new UpdateSubCategoryAsyncTask(subCategoryDao).execute(subCategory);
    }

    public void deleteSubCategory(SubCategory subCategory) {
        new DeleteSubCategoryAsyncTask(subCategoryDao).execute(subCategory);
    }

    public LiveData<List<SubCategory>> getSubCategoriesByCategoryId(long categoryId) {
        return subCategoryDao.getSubCategoriesByCategoryId(categoryId);
    }

    public SubCategory getSubCategoryByName(long categoryId, String name) {
        return subCategoryDao.getSubCategoryByName(categoryId, name);
    }

    // ==================== 位置相关操作 ====================
    public void insertLocation(Location location) {
        new InsertLocationAsyncTask(locationDao).execute(location);
    }

    public void updateLocation(Location location) {
        new UpdateLocationAsyncTask(locationDao).execute(location);
    }

    public void deleteLocation(Location location) {
        new DeleteLocationAsyncTask(locationDao).execute(location);
    }

    public LiveData<List<Location>> getAllLocations() {
        return allLocations;
    }

    public Location getLocationByName(String name) {
        return locationDao.getLocationByName(name);
    }

    // ==================== 使用记录相关操作 ====================
    public LiveData<List<UsageRecord>> getRecordsByItemId(String itemId) {
        return usageRecordDao.getRecordsByItemId(itemId);
    }

    // ==================== 异步任务类（AsyncTask，兼容API 28+） ====================
    // 插入物品 + 插入创建记录
    private static class InsertItemAsyncTask extends AsyncTask<Object, Void, Void> {
        private ItemDao itemDao;
        private UsageRecordDao usageRecordDao;

        public InsertItemAsyncTask(ItemDao itemDao, UsageRecordDao usageRecordDao) {
            this.itemDao = itemDao;
            this.usageRecordDao = usageRecordDao;
        }

        @Override
        protected Void doInBackground(Object... objects) {
            Item item = (Item) objects[0];
            String createTime = (String) objects[1];
            // 插入物品
            itemDao.insert(item);
            // 插入创建记录
            UsageRecord record = new UsageRecord(
                    item.getId(),
                    "create",
                    createTime,
                    "" // 创建记录无修改字段
            );
            usageRecordDao.insert(record);
            return null;
        }
    }

    // 更新物品 + 插入修改记录
    private static class UpdateItemAsyncTask extends AsyncTask<Object, Void, Void> {
        private ItemDao itemDao;
        private UsageRecordDao usageRecordDao;

        public UpdateItemAsyncTask(ItemDao itemDao, UsageRecordDao usageRecordDao) {
            this.itemDao = itemDao;
            this.usageRecordDao = usageRecordDao;
        }

        @Override
        protected Void doInBackground(Object... objects) {
            Item newItem = (Item) objects[0];
            String updateTime = (String) objects[1];
            String modifiedFields = (String) objects[2];
            // 更新物品
            itemDao.update(newItem);
            // 插入修改记录
            UsageRecord record = new UsageRecord(
                    newItem.getId(),
                    "update",
                    updateTime,
                    modifiedFields
            );
            usageRecordDao.insert(record);
            return null;
        }
    }

    // 删除物品 + 删除关联记录
    private static class DeleteItemAsyncTask extends AsyncTask<Item, Void, Void> {
        private ItemDao itemDao;
        private UsageRecordDao usageRecordDao;

        public DeleteItemAsyncTask(ItemDao itemDao, UsageRecordDao usageRecordDao) {
            this.itemDao = itemDao;
            this.usageRecordDao = usageRecordDao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            Item item = items[0];
            // 删除物品
            itemDao.delete(item);
            // 删除关联的使用记录
            usageRecordDao.deleteRecordsByItemId(item.getId());
            return null;
        }
    }

    // 分类异步任务（插入/更新/删除）
    private static class InsertCategoryAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDao categoryDao;

        public InsertCategoryAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDao.insert(categories[0]);
            return null;
        }
    }

    private static class UpdateCategoryAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDao categoryDao;

        public UpdateCategoryAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDao.update(categories[0]);
            return null;
        }
    }

    private static class DeleteCategoryAsyncTask extends AsyncTask<Category, Void, Void> {
        private CategoryDao categoryDao;

        public DeleteCategoryAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Void doInBackground(Category... categories) {
            categoryDao.delete(categories[0]);
            return null;
        }
    }

    // 子分类异步任务（插入/更新/删除）
    private static class InsertSubCategoryAsyncTask extends AsyncTask<SubCategory, Void, Void> {
        private SubCategoryDao subCategoryDao;

        public InsertSubCategoryAsyncTask(SubCategoryDao subCategoryDao) {
            this.subCategoryDao = subCategoryDao;
        }

        @Override
        protected Void doInBackground(SubCategory... subCategories) {
            subCategoryDao.insert(subCategories[0]);
            return null;
        }
    }

    private static class UpdateSubCategoryAsyncTask extends AsyncTask<SubCategory, Void, Void> {
        private SubCategoryDao subCategoryDao;

        public UpdateSubCategoryAsyncTask(SubCategoryDao subCategoryDao) {
            this.subCategoryDao = subCategoryDao;
        }

        @Override
        protected Void doInBackground(SubCategory... subCategories) {
            subCategoryDao.update(subCategories[0]);
            return null;
        }
    }

    private static class DeleteSubCategoryAsyncTask extends AsyncTask<SubCategory, Void, Void> {
        private SubCategoryDao subCategoryDao;

        public DeleteSubCategoryAsyncTask(SubCategoryDao subCategoryDao) {
            this.subCategoryDao = subCategoryDao;
        }

        @Override
        protected Void doInBackground(SubCategory... subCategories) {
            subCategoryDao.delete(subCategories[0]);
            return null;
        }
    }

    // 位置异步任务（插入/更新/删除）
    private static class InsertLocationAsyncTask extends AsyncTask<Location, Void, Void> {
        private LocationDao locationDao;

        public InsertLocationAsyncTask(LocationDao locationDao) {
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.insert(locations[0]);
            return null;
        }
    }

    private static class UpdateLocationAsyncTask extends AsyncTask<Location, Void, Void> {
        private LocationDao locationDao;

        public UpdateLocationAsyncTask(LocationDao locationDao) {
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.update(locations[0]);
            return null;
        }
    }

    private static class DeleteLocationAsyncTask extends AsyncTask<Location, Void, Void> {
        private LocationDao locationDao;

        public DeleteLocationAsyncTask(LocationDao locationDao) {
            this.locationDao = locationDao;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            locationDao.delete(locations[0]);
            return null;
        }
    }
}