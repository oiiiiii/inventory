package com.example.inventory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {
    // 传递物品ID的Key
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    // UI控件
    private TextView mTvItemName;
    private TextView mTvCategory;
    private TextView mTvLocation;
    private TextView mTvQuantity;
    private TextView mTvExpiryDate;
    private TextView mTvExpireTip;
    private TextView mTvDescription;
    private TextView mTvCreateTime;
    private TextView mTvUpdateTime;
    private Button mBtnEdit;
    private Button mBtnDelete;

    // 新增：图片预览
    private RecyclerView mRvItemImages;
    private ImagePreviewAdapter mImageAdapter;
    // ViewModel
    private InventoryViewModel mViewModel;

    // 当前物品
    private Item mCurrentItem;

    // 日期格式化器
    private SimpleDateFormat mExpiryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat mDisplayFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
    private static final int NEAR_EXPIRE_DAYS = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // 初始化UI控件
        initView();

        // 初始化ViewModel
        initViewModel();

        // 获取传递的物品ID并加载数据
        loadItemData();

        // 设置点击事件
        setClickEvents();
    }

    /**
     * 初始化UI控件
     */
    private void initView() {
        mTvItemName = findViewById(R.id.tv_item_name);
        mTvCategory = findViewById(R.id.tv_category);
        mTvLocation = findViewById(R.id.tv_location);
        mTvQuantity = findViewById(R.id.tv_quantity);
        mTvExpiryDate = findViewById(R.id.tv_expiry_date);
        mTvExpireTip = findViewById(R.id.tv_expire_tip);
        mTvDescription = findViewById(R.id.tv_description);
        mTvCreateTime = findViewById(R.id.tv_create_time);
        mTvUpdateTime = findViewById(R.id.tv_update_time);
        mBtnEdit = findViewById(R.id.btn_edit);
        mBtnDelete = findViewById(R.id.btn_delete);

        // 新增：图片预览
        mRvItemImages = findViewById(R.id.rv_item_images);
        mImageAdapter = new ImagePreviewAdapter(this, new ArrayList<>());
        mRvItemImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRvItemImages.setAdapter(mImageAdapter);
    }

    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
    }

    private void loadItemData() {
        String itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "物品ID为空！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mViewModel.getItemById(itemId).observe(this, new Observer<Item>() {
            @Override
            public void onChanged(Item item) {
                if (item == null) {
                    Toast.makeText(ItemDetailActivity.this, "物品不存在！", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                mCurrentItem = item;
                bindItemData(item);

                // 新增：加载图片（使用辅助方法）
                List<String> imagePaths = item.getImagePathsList();  // 改为使用辅助方法
                mImageAdapter.refreshData(imagePaths);
            }
        });
    }

    /**
     * 绑定物品数据到UI
     */
    private void bindItemData(Item item) {
        // 基础信息
        mTvItemName.setText(item.getName());

        // 分类（拼接子分类）
        String category = item.getCategory() == null || item.getCategory().isEmpty() ? "未分类" : item.getCategory();
        String subCategory = item.getSubCategory() == null || item.getSubCategory().isEmpty() ? "" : "/" + item.getSubCategory();
        mTvCategory.setText(category + subCategory);

        // 位置
        mTvLocation.setText(item.getLocation() == null || item.getLocation().isEmpty() ? "未指定" : item.getLocation());

        // 数量
        mTvQuantity.setText(item.getQuantity() + "");

        // 有效期 + 临期/过期提醒
        String expiryDate = item.getExpiryDate();
        if (expiryDate == null || expiryDate.isEmpty()) {
            mTvExpiryDate.setText("无");
            mTvExpireTip.setVisibility(View.GONE);
        } else {
            try {
                Date expiryDateObj = mExpiryFormat.parse(expiryDate);
                mTvExpiryDate.setText(mDisplayFormat.format(expiryDateObj));

                // 判断临期/过期
                Calendar today = Calendar.getInstance();
                Calendar expiryCal = Calendar.getInstance();
                expiryCal.setTime(expiryDateObj);

                long diff = expiryCal.getTimeInMillis() - today.getTimeInMillis();
                long diffDays = diff / (1000 * 60 * 60 * 24);

                if (diffDays < 0) {
                    mTvExpireTip.setText("已过期");
                    mTvExpireTip.setBackgroundColor(getResources().getColor(R.color.warning_expired));
                    mTvExpireTip.setVisibility(View.VISIBLE);
                } else if (diffDays <= NEAR_EXPIRE_DAYS) {
                    mTvExpireTip.setText("临期");
                    mTvExpireTip.setBackgroundColor(getResources().getColor(R.color.warning_near_expire));
                    mTvExpireTip.setVisibility(View.VISIBLE);
                } else {
                    mTvExpireTip.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                mTvExpiryDate.setText(expiryDate);
                mTvExpireTip.setVisibility(View.GONE);
            }
        }

        // 描述
        mTvDescription.setText(item.getDescription() == null || item.getDescription().isEmpty() ? "无" : item.getDescription());

        // 时间
        mTvCreateTime.setText(item.getCreateTime());
        mTvUpdateTime.setText(item.getUpdateTime());
    }

    /**
     * 设置点击事件
     */
    private void setClickEvents() {
        // 编辑按钮：跳转到AddItemActivity（携带物品数据）
        mBtnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ItemDetailActivity.this, AddItemActivity.class);
            intent.putExtra(EXTRA_ITEM_ID, mCurrentItem.getId());
            startActivity(intent);
            finish(); // 关闭详情页，返回后重新加载数据
        });

        // 删除按钮：弹出确认框，确认后删除
        mBtnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("是否确定删除该物品？删除后无法恢复！")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 调用ViewModel删除物品
                        mViewModel.deleteItem(mCurrentItem);
                        Toast.makeText(ItemDetailActivity.this, "物品删除成功！", Toast.LENGTH_SHORT).show();
                        finish(); // 关闭详情页，返回主页面
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        // 新增：图片预览点击（放大）
        mImageAdapter.setOnImageClickListener(position -> {
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            // 使用辅助方法获取图片路径列表
            intent.putStringArrayListExtra("imagePaths", new ArrayList<>(mCurrentItem.getImagePathsList()));
            intent.putExtra("position", position);
            startActivity(intent);
        });

        // 详情页图片不显示删除按钮
        mImageAdapter.setOnImageDeleteListener(null);
    }
}