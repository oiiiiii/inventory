package com.example.inventory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 视图模型：管理界面状态，提供数据给UI层
 */
public class InventoryViewModel extends AndroidViewModel {
    // 仓库实例
    private InventoryRepository repository;

    // 界面状态：物品列表（分页加载）
    private MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>();
    // 界面状态：加载中（true=加载中，false=加载完成）
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    // 界面状态：无数据（true=无数据，false=有数据）
    private MutableLiveData<Boolean> isEmptyLiveData = new MutableLiveData<>();
    // 分页参数
    private int pageSize = 20; // 每页20条
    private int currentPage = 1; // 当前页码（从1开始）
    private boolean hasMoreData = true; // 是否还有更多数据

    // 全局LiveData（分类、位置列表）
    private LiveData<List<Category>> allCategories;
    private LiveData<List<Location>> allLocations;

    // 日期格式化（用于生成创建/修改时间）
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        // 初始化仓库
        repository = new InventoryRepository(application);
        // 初始化全局LiveData
        allCategories = repository.getAllCategories();
        allLocations = repository.getAllLocations();
    }

    // ==================== 物品相关操作 ====================
    // 插入物品
    public void insertItem(Item item) {
        String currentTime = dateFormat.format(new Date());
        repository.insertItem(item, currentTime);
    }

    // 更新物品（需传入修改的字段描述，如"名称、分类"）
    public void updateItem(Item oldItem, Item newItem, String modifiedFields) {
        String currentTime = dateFormat.format(new Date());
        repository.updateItem(oldItem, newItem, currentTime, modifiedFields);
    }

    // 删除物品
    public void deleteItem(Item item) {
        repository.deleteItem(item);
    }

    // 根据ID查询物品
    public LiveData<Item> getItemById(String itemId) {
        return repository.getItemById(itemId);
    }

    // 获取物品使用记录
    public LiveData<List<UsageRecord>> getRecordsByItemId(String itemId) {
        return repository.getRecordsByItemId(itemId);
    }

    // ==================== 分页加载相关 ====================
    // 加载第一页数据（初始化/刷新）
    public void loadFirstPage() {
        currentPage = 1;
        hasMoreData = true;
        loadData(currentPage);
    }

    // 加载下一页数据（滑动到底部）
    public void loadNextPage() {
        if (isLoadingLiveData.getValue() == null || isLoadingLiveData.getValue()) {
            return; // 正在加载中，不重复请求
        }
        if (!hasMoreData) {
            return; // 没有更多数据
        }
        currentPage++;
        loadData(currentPage);
    }

    // 加载数据（通用分页方法）
    private void loadData(int page) {
        isLoadingLiveData.setValue(true);
        int offset = (page - 1) * pageSize; // 计算偏移量（从0开始）

        // 这里先实现"加载所有物品"，后续可扩展筛选/搜索逻辑
        repository.getItemsByPage(pageSize, offset).observeForever(items -> {
            isLoadingLiveData.setValue(false);
            if (items == null || items.isEmpty()) {
                if (page == 1) {
                    isEmptyLiveData.setValue(true); // 第一页无数据
                } else {
                    hasMoreData = false; // 后续页无数据，标记无更多
                }
                return;
            }

            isEmptyLiveData.setValue(false);
            List<Item> currentList = itemListLiveData.getValue();
            if (currentList == null || page == 1) {
                // 第一页：直接赋值
                itemListLiveData.setValue(items);
            } else {
                // 后续页：追加数据
                currentList.addAll(items);
                itemListLiveData.setValue(currentList);
            }

            // 如果当前页数据少于pageSize，标记无更多数据
            if (items.size() < pageSize) {
                hasMoreData = false;
            }
        });
    }

    // 筛选临期物品
    public void filterExpiringItems(int days) {
        currentPage = 1;
        hasMoreData = true;
        isLoadingLiveData.setValue(true);
        int offset = (currentPage - 1) * pageSize;

        repository.getExpiringItems(days, pageSize, offset).observeForever(items -> {
            handleFilterData(items, currentPage);
        });
    }

    // 筛选过期物品
    public void filterExpiredItems() {
        currentPage = 1;
        hasMoreData = true;
        isLoadingLiveData.setValue(true);
        int offset = (currentPage - 1) * pageSize;

        repository.getExpiredItems(pageSize, offset).observeForever(items -> {
            handleFilterData(items, currentPage);
        });
    }

    // 搜索物品（按名称模糊查询）
    public void searchItemsByName(String keyword) {
        currentPage = 1;
        hasMoreData = true;
        isLoadingLiveData.setValue(true);
        int offset = (currentPage - 1) * pageSize;

        repository.searchItemsByName(keyword, pageSize, offset).observeForever(items -> {
            handleFilterData(items, currentPage);
        });
    }

    // 处理筛选/搜索结果（通用方法）
    private void handleFilterData(List<Item> items, int page) {
        isLoadingLiveData.setValue(false);
        if (items == null || items.isEmpty()) {
            if (page == 1) {
                isEmptyLiveData.setValue(true);
                itemListLiveData.setValue(null);
            } else {
                hasMoreData = false;
            }
            return;
        }

        isEmptyLiveData.setValue(false);
        List<Item> currentList = itemListLiveData.getValue();
        if (currentList == null || page == 1) {
            itemListLiveData.setValue(items);
        } else {
            currentList.addAll(items);
            itemListLiveData.setValue(currentList);
        }

        if (items.size() < pageSize) {
            hasMoreData = false;
        }
    }

    // ==================== 分类相关操作 ====================
    public void insertCategory(Category category) {
        repository.insertCategory(category);
    }

    public void updateCategory(Category category) {
        repository.updateCategory(category);
    }

    public void deleteCategory(Category category) {
        repository.deleteCategory(category);
    }

    public Category getCategoryByName(String name) {
        return repository.getCategoryByName(name);
    }

    // ==================== 子分类相关操作 ====================
    public void insertSubCategory(SubCategory subCategory) {
        repository.insertSubCategory(subCategory);
    }

    public void updateSubCategory(SubCategory subCategory) {
        repository.updateSubCategory(subCategory);
    }

    public void deleteSubCategory(SubCategory subCategory) {
        repository.deleteSubCategory(subCategory);
    }

    public LiveData<List<SubCategory>> getSubCategoriesByCategoryId(long categoryId) {
        return repository.getSubCategoriesByCategoryId(categoryId);
    }

    public SubCategory getSubCategoryByName(long categoryId, String name) {
        return repository.getSubCategoryByName(categoryId, name);
    }

    // ==================== 位置相关操作 ====================
    public void insertLocation(Location location) {
        repository.insertLocation(location);
    }

    public void updateLocation(Location location) {
        repository.updateLocation(location);
    }

    public void deleteLocation(Location location) {
        repository.deleteLocation(location);
    }

    public Location getLocationByName(String name) {
        return repository.getLocationByName(name);
    }

    // ==================== LiveData getter（UI层观察） ====================
    public LiveData<List<Item>> getItemListLiveData() {
        return itemListLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<Boolean> getIsEmptyLiveData() {
        return isEmptyLiveData;
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Location>> getAllLocations() {
        return allLocations;
    }

    public boolean isHasMoreData() {
        return hasMoreData;
    }
}