package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {
    // UI控件
    private EditText mEtItemName;
    private Spinner mSpCategory;
    private Spinner mSpSubCategory;
    private Spinner mSpLocation;
    private EditText mEtQuantity;
    private EditText mEtExpiryDate;
    private Button mBtnSelectDate;
    private Button mBtnSubmit;
    private EditText mEtDescription;
    private TextView mTvPageTitle; // 直接获取标题控件，避免重复findViewById
    // 新增：图片相关控件
    private Button mBtnSelectImage;
    private RecyclerView mRvImagePreview;
    private ImagePreviewAdapter mImageAdapter;
    private List<String> mImagePaths = new ArrayList<>();

    // ViewModel
    private InventoryViewModel mViewModel;

    // 日期格式化器
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar mCalendar = Calendar.getInstance();

    // 编辑模式标记
    private boolean isEditMode = false;
    private Item mEditItem; // 待编辑的物品

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 初始化UI控件（直接获取标题控件）
        initView();

        // 初始化ViewModel
        initViewModel();

        // 检查是否是编辑模式
        checkEditMode();

        // 设置点击事件
        setClickEvents();
    }

    /**
     * 初始化UI控件
     */
    private void initView() {
        mTvPageTitle = findViewById(R.id.tv_page_title); // 直接获取标题
        mEtItemName = findViewById(R.id.et_item_name);
        mSpCategory = findViewById(R.id.sp_category);
        mSpSubCategory = findViewById(R.id.sp_sub_category);
        mSpLocation = findViewById(R.id.sp_location);
        mEtQuantity = findViewById(R.id.et_quantity);
        mEtExpiryDate = findViewById(R.id.et_expiry_date);
        mBtnSelectDate = findViewById(R.id.btn_select_date);
        mBtnSubmit = findViewById(R.id.btn_submit);
        mEtDescription = findViewById(R.id.et_description);
        // 新增：图片控件
        mBtnSelectImage = findViewById(R.id.btn_select_image);
        mRvImagePreview = findViewById(R.id.rv_image_preview);
        // 初始化图片预览RecyclerView
        mImageAdapter = new ImagePreviewAdapter(this, mImagePaths);
        mRvImagePreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRvImagePreview.setAdapter(mImageAdapter);
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
    }

    /**
     * 检查是否是编辑模式（是否携带物品ID）
     */
    private void checkEditMode() {
        String itemId = getIntent().getStringExtra(ItemDetailActivity.EXTRA_ITEM_ID);
        if (itemId != null && !itemId.isEmpty()) {
            // 编辑模式
            isEditMode = true;
            // 修改页面标题和按钮文字（立即修改，无需等数据加载）
            mTvPageTitle.setText("编辑物品");
            mBtnSubmit.setText("保存修改");

            // 加载待编辑的物品数据（关键：添加Observer监听数据）
            mViewModel.getItemById(itemId).observe(this, new Observer<Item>() {
                @Override
                public void onChanged(Item item) {
                    if (item == null) {
                        Toast.makeText(AddItemActivity.this, "物品数据加载失败！", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    mEditItem = item;
                    // 回显数据到表单
                    fillFormData(item);
                }
            });
        }
    }

    /**
     * 回显物品数据到表单
     */
    private void fillFormData(Item item) {
        // 物品名称
        mEtItemName.setText(item.getName() != null ? item.getName() : "");

        // 分类（匹配Spinner选项，兼容空值）
        String category = item.getCategory() == null || item.getCategory().isEmpty() ? "未分类" : item.getCategory();
        setSpinnerSelection(mSpCategory, category);

        // 子分类
        String subCategory = item.getSubCategory() == null || item.getSubCategory().isEmpty() ? "未指定" : item.getSubCategory();
        setSpinnerSelection(mSpSubCategory, subCategory);

        // 位置
        String location = item.getLocation() == null || item.getLocation().isEmpty() ? "未指定" : item.getLocation();
        setSpinnerSelection(mSpLocation, location);

        // 数量
        mEtQuantity.setText(String.valueOf(item.getQuantity()));

        // 有效期
        mEtExpiryDate.setText(item.getExpiryDate() != null ? item.getExpiryDate() : "");

        // 描述
        mEtDescription.setText(item.getDescription() != null ? item.getDescription() : "");

        // 新增：回显图片路径
        if (item.getImagePaths() != null && !item.getImagePaths().isEmpty()) {
            mImagePaths = item.getImagePathsList();  // 使用辅助方法获取列表
            mImageAdapter.refreshData(mImagePaths);
        }
    }

    /**
     * 设置Spinner选中指定值（兼容未找到的情况）
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            String item = spinner.getAdapter().getItem(i).toString();
            if (item.equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
        // 未找到则选中第一个（默认值）
        spinner.setSelection(0);
    }

    /**
     * 设置点击事件
     */
    private void setClickEvents() {
        // 日期选择按钮
        mBtnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // 新增：图片选择按钮
        mBtnSelectImage.setOnClickListener(v -> {
            // 检查权限
            if (PermissionUtils.checkAndRequestPermissions(this)) {
                // 权限已授予，显示选择菜单
                showImageSelectMenu();
            }
        });

        // 新增：图片预览点击/删除监听
        mImageAdapter.setOnImageClickListener(position -> {
            // 点击图片：跳转到图片预览页（可放大）
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putStringArrayListExtra("imagePaths", new ArrayList<>(mImagePaths));
            intent.putExtra("position", position);
            startActivity(intent);
        });

        mImageAdapter.setOnImageDeleteListener(position -> {
            // 删除图片
            mImagePaths.remove(position);
            mImageAdapter.refreshData(mImagePaths);
            // 编辑模式下更新物品图片列表
            if (isEditMode && mEditItem != null) {
                mEditItem.setImagePathsList(mImagePaths);  // 改为使用辅助方法
            }
        });

        // 提交按钮
        mBtnSubmit.setOnClickListener(v -> submitItem());
    }

    /**
     * 显示图片选择菜单（拍照/图库）
     */
    private void showImageSelectMenu() {
        new AlertDialog.Builder(this)
                .setTitle("选择图片来源")
                .setItems(new String[]{"拍照", "从图库选择"}, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        ImageUtils.openCamera(this);
                    } else {
                        // 图库选择
                        ImageUtils.openGallery(this);
                    }
                })
                .show();
    }

    /**
     * 显示日期选择器
     */
    private void showDatePickerDialog() {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // 格式化日期并显示
                mEtExpiryDate.setText(mDateFormat.format(mCalendar.getTime()));
            }
        },
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 提交物品数据（修复编辑模式逻辑）
     */
    private void submitItem() {
        // 1. 表单校验（原有逻辑不变）
        String itemName = mEtItemName.getText().toString().trim();
        String quantityStr = mEtQuantity.getText().toString().trim();

        if (itemName.isEmpty()) {
            Toast.makeText(this, "物品名称不能为空！", Toast.LENGTH_SHORT).show();
            mEtItemName.requestFocus();
            return;
        }

        int quantity = 0;
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "物品数量不能为空！", Toast.LENGTH_SHORT).show();
            mEtQuantity.requestFocus();
            return;
        }
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "物品数量必须是正整数！", Toast.LENGTH_SHORT).show();
                mEtQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "物品数量格式错误！", Toast.LENGTH_SHORT).show();
            mEtQuantity.requestFocus();
            return;
        }

        // 2. 收集表单数据
        String category = mSpCategory.getSelectedItem().toString();
        String subCategory = mSpSubCategory.getSelectedItem().toString();
        String location = mSpLocation.getSelectedItem().toString();
        String expiryDate = mEtExpiryDate.getText().toString().trim();
        String description = mEtDescription.getText().toString().trim();

        // 3. 区分新增/编辑模式（核心修复）
        if (isEditMode && mEditItem != null) {
            // 编辑模式：更新原有物品（复用ID，只改字段）
            mEditItem.setName(itemName);
            mEditItem.setCategory(category.equals("未分类") ? "" : category);
            mEditItem.setSubCategory(subCategory.equals("未指定") ? "" : subCategory);
            mEditItem.setLocation(location.equals("未指定") ? "" : location);
            mEditItem.setQuantity(quantity);
            mEditItem.setExpiryDate(expiryDate);
            mEditItem.setDescription(description);

            // 设置图片路径（重要！）
            if (!mImagePaths.isEmpty()) {
                mEditItem.setImagePathsList(mImagePaths);  // 使用辅助方法
            }

            // 调用ViewModel更新物品（关键：传入原有Item对象）
            mViewModel.updateItem(mEditItem);
            Toast.makeText(this, "物品修改成功！", Toast.LENGTH_SHORT).show();
        } else {
            // 新增模式：创建新物品
            Item item = new Item();
            item.setName(itemName);
            item.setCategory(category.equals("未分类") ? "" : category);
            item.setSubCategory(subCategory.equals("未指定") ? "" : subCategory);
            item.setLocation(location.equals("未指定") ? "" : location);
            item.setQuantity(quantity);
            item.setExpiryDate(expiryDate);
            item.setDescription(description);

            // 设置图片路径（重要！）
            if (!mImagePaths.isEmpty()) {
                item.setImagePathsList(mImagePaths);  // 使用辅助方法
            }

            // 调用ViewModel保存物品
            mViewModel.insertItem(item);
            Toast.makeText(this, "物品添加成功！", Toast.LENGTH_SHORT).show();
        }

        // 4. 返回主页面
        finish();
    }

    /**
     * 权限申请结果回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSIONS) {
            if (PermissionUtils.verifyPermissions(grantResults)) {
                // 权限申请成功，显示图片选择菜单
                showImageSelectMenu();
            } else {
                Toast.makeText(this, "需要相机和存储权限才能选择图片！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 图片选择/拍照结果回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String imagePath = null;
            if (requestCode == ImageUtils.REQUEST_CODE_CAMERA) {
                // 拍照返回
                imagePath = ImageUtils.handleCameraResult();
            } else if (requestCode == ImageUtils.REQUEST_CODE_GALLERY && data != null) {
                // 图库返回
                imagePath = ImageUtils.handleGalleryResult(this, data.getData());
            }

            // 添加图片路径并刷新预览
            if (imagePath != null && !imagePath.isEmpty()) {
                mImagePaths.add(imagePath);
                mImageAdapter.refreshData(mImagePaths);
                // 编辑模式下更新物品图片列表
                if (isEditMode && mEditItem != null) {
                    mEditItem.setImagePathsList(mImagePaths);  // 改为使用辅助方法
                }
            }
        }
    }
}