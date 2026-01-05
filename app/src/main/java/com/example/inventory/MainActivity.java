package com.example.inventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import android.content.Intent; // 新增导入

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // UI控件
    private ProgressBar mProgressBar;
    private LinearLayout mLlEmpty;
    private RecyclerView mRvItemList;
    private TextView mTvLoadMore;
    private FloatingActionButton mFabAddItem;

    // ViewModel
    private InventoryViewModel mViewModel;

    // 适配器
    private ItemAdapter mItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI控件
        initView();

        // 初始化ViewModel
        initViewModel();

        // 初始化RecyclerView
        initRecyclerView();

        // 设置点击事件
        setClickEvents();

        // 加载第一页数据
        mViewModel.loadFirstPage();
    }

    /**
     * 初始化UI控件
     */
    private void initView() {
        mProgressBar = findViewById(R.id.progress_bar);
        mLlEmpty = findViewById(R.id.ll_empty);
        mRvItemList = findViewById(R.id.rv_item_list);
        mTvLoadMore = findViewById(R.id.tv_load_more);
        mFabAddItem = findViewById(R.id.fab_add_item);
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // 观察加载状态
        mViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (mViewModel.getItemListLiveData().getValue() == null || mViewModel.getItemListLiveData().getValue().isEmpty()) {
                    mLlEmpty.setVisibility(View.GONE);
                    mRvItemList.setVisibility(View.GONE);
                } else {
                    mTvLoadMore.setVisibility(View.VISIBLE);
                }
            } else {
                mProgressBar.setVisibility(View.GONE);
                mTvLoadMore.setVisibility(View.GONE);
            }
        });

        // 观察空状态
        mViewModel.getIsEmptyLiveData().observe(this, isEmpty -> {
            if (isEmpty) {
                mLlEmpty.setVisibility(View.VISIBLE);
                mRvItemList.setVisibility(View.GONE);
            } else {
                mLlEmpty.setVisibility(View.GONE);
                mRvItemList.setVisibility(View.VISIBLE);
            }
        });

        // 观察物品列表数据
        mViewModel.getItemListLiveData().observe(this, items -> {
            if (items == null || items.isEmpty()) {
                return;
            }

            // 第一页数据：更新适配器
            if (mViewModel.getItemListLiveData().getValue() != null && mViewModel.getItemListLiveData().getValue().size() == items.size()) {
                mItemAdapter.updateData(items);
            } else {
                // 后续页数据：追加
                mItemAdapter.addData(items);
            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRvItemList.setLayoutManager(layoutManager);
        mItemAdapter = new ItemAdapter(this, new ArrayList<>(), item -> {
            // 物品条目点击事件（后续跳转到详情页）
            // 暂未实现，先留空
        });
        mRvItemList.setAdapter(mItemAdapter);

        // 监听滑动到底部，加载下一页
        mRvItemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItemPos = lm.findLastVisibleItemPosition();
                int totalItemCount = lm.getItemCount();

                // 滑动到底部 + 有更多数据 + 不在加载中
                if (lastVisibleItemPos == totalItemCount - 1
                        && mViewModel.isHasMoreData()
                        && (mViewModel.getIsLoadingLiveData().getValue() == null || !mViewModel.getIsLoadingLiveData().getValue())) {
                    mViewModel.loadNextPage();
                }
            }
        });
    }

    /**
     * 设置点击事件
     */
    private void setClickEvents() {
        // 添加物品按钮点击（跳转到录入页）
        mFabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });
    }
}