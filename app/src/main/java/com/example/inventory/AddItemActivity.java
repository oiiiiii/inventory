package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    // ViewModel
    private InventoryViewModel mViewModel;

    // 日期格式化器
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar mCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 初始化UI控件
        initView();

        // 初始化ViewModel
        initViewModel();

        // 设置点击事件
        setClickEvents();
    }

    /**
     * 初始化UI控件
     */
    private void initView() {
        mEtItemName = findViewById(R.id.et_item_name);
        mSpCategory = findViewById(R.id.sp_category);
        mSpSubCategory = findViewById(R.id.sp_sub_category);
        mSpLocation = findViewById(R.id.sp_location);
        mEtQuantity = findViewById(R.id.et_quantity);
        mEtExpiryDate = findViewById(R.id.et_expiry_date);
        mBtnSelectDate = findViewById(R.id.btn_select_date);
        mBtnSubmit = findViewById(R.id.btn_submit);
        mEtDescription = findViewById(R.id.et_description);
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
    }

    /**
     * 设置点击事件
     */
    private void setClickEvents() {
        // 日期选择按钮
        mBtnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // 提交按钮
        mBtnSubmit.setOnClickListener(v -> submitItem());
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
     * 提交物品数据
     */
    private void submitItem() {
        // 1. 表单校验
        String itemName = mEtItemName.getText().toString().trim();
        String quantityStr = mEtQuantity.getText().toString().trim();

        // 名称不能为空
        if (itemName.isEmpty()) {
            Toast.makeText(this, "物品名称不能为空！", Toast.LENGTH_SHORT).show();
            mEtItemName.requestFocus();
            return;
        }

        // 数量不能为空且必须是正整数
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

        // 3. 创建Item对象
        Item item = new Item();
        item.setName(itemName);
        item.setCategory(category.equals("未分类") ? "" : category);
        item.setSubCategory(subCategory.equals("未指定") ? "" : subCategory);
        item.setLocation(location.equals("未指定") ? "" : location);
        item.setQuantity(quantity);
        item.setExpiryDate(expiryDate);
        item.setDescription(description);

        // 4. 调用ViewModel保存物品
        mViewModel.insertItem(item);

        // 5. 提示并返回主页面
        Toast.makeText(this, "物品添加成功！", Toast.LENGTH_SHORT).show();
        finish(); // 关闭当前页面，返回主页面
    }
}